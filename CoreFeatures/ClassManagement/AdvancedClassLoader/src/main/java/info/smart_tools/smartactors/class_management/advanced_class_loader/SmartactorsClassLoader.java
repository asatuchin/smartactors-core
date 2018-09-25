package info.smart_tools.smartactors.class_management.advanced_class_loader;

import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extension of {@link URLClassLoader}
 */
public class SmartactorsClassLoader extends URLClassLoader implements ISmartactorsClassLoader {

    private static Object defaultItemId = null;

    /* This is ItemID To ClassLoader Map */
    private static Map<Object, SmartactorsClassLoader> itemClassLoaders = new ConcurrentHashMap<>();

    private String itemName = null;
    private Set<SmartactorsClassLoader> dependsOn = Collections.synchronizedSet(new HashSet<>());
    private Map<String, ClassLoader> classMap = new ConcurrentHashMap<>();

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    private SmartactorsClassLoader(final URL[] urls) {
        super(urls);
    }

    public static void setDefaultItemId(Object itemID) {
        defaultItemId = itemID;
    }

    public static void addItem(Object itemID, String itemName, String itemVersion) {
        SmartactorsClassLoader classLoader = new SmartactorsClassLoader(new URL[]{});
        itemClassLoaders.put(itemID, classLoader);
        itemName = itemName.replace('/', '.');
        itemName = itemName.replace(':', '.');
        itemName = itemName.replace('-', '_');
        classLoader.itemName = itemName;
        classLoader.classMap.put(itemName, classLoader);
    }

    public static SmartactorsClassLoader getItemClassLoader(Object itemID) {
        return itemClassLoaders.get(itemID);
    }

    public static void addItemDependency(Object dependentItemID, Object baseItemID) {
        if (!baseItemID.equals(dependentItemID)) {
            SmartactorsClassLoader baseClassLoader = getItemClassLoader(baseItemID);
            SmartactorsClassLoader dependentClassLoader = getItemClassLoader(dependentItemID);
            if (baseClassLoader != null && dependentClassLoader != null) {
                Set<SmartactorsClassLoader> classLoaders = new HashSet<SmartactorsClassLoader>();
                classLoaders.add(baseClassLoader);
                classLoaders.addAll(baseClassLoader.dependsOn);
                for (SmartactorsClassLoader cl : classLoaders) {
                    dependentClassLoader.classMap.put(cl.itemName, cl);
                }
                dependentClassLoader.dependsOn.addAll(classLoaders);
            }
        }
    }

    public static void finalizeItemDependencies(Object itemID) {
        if (getItemClassLoader(itemID).dependsOn.size() == 0) {
            addItemDependency(itemID, defaultItemId);
        }
    }

    public URL[] getURLsFromDependencies() {

        Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
        classLoaders.add(this);
        classLoaders.addAll(dependsOn);
        for(ClassLoader classLoader : dependsOn) {
            ClassLoader parent = classLoader.getParent();
            while(parent != null) {
                classLoaders.add(parent);
                parent = parent.getParent();
            }
        }

        ArrayList<URL> urlArrayList = new ArrayList<>();
        for( ClassLoader classLoader : classLoaders) {
            if (classLoader instanceof URLClassLoader) {
                Collections.addAll(urlArrayList, ((URLClassLoader) classLoader).getURLs());
            }
        }

        URL[] urls = new URL[urlArrayList.size()];
        urlArrayList.toArray(urls);

        return urls;
    }

    private Class<?> loadClass0(String className, boolean upperLevel)
            throws ClassNotFoundException {

        Class clazz = this.findLoadedClass(className);
        if (clazz == null) {

            ClassLoader classLoader = classMap.get(className);
            if (classLoader != null) {
                try {
                    clazz = classLoader.loadClass(className);
                } catch (ClassNotFoundException e) { }
            }

            if (clazz == null) {
                if (this.getParent() != null) {
                    try {
                        clazz = this.getParent().loadClass(className);
                    } catch (ClassNotFoundException e) { }
                }

                if (clazz == null && upperLevel) {
                    String name = className;
                    do {
                        int index = name.lastIndexOf(".");
                        if (index == -1) {
                            break;
                        }
                        name = name.substring(0, index);
                        classLoader = classMap.get(name);
                    } while (classLoader == null);

                    if (classLoader != null && this != classLoader) {
                        try {
                            clazz = classLoader.loadClass(className);
                            classMap.put(className, clazz.getClassLoader());
                        } catch (ClassNotFoundException e) { }
                    }
                }

                if (clazz == null && upperLevel) {
                    for (SmartactorsClassLoader dependency : dependsOn) {
                        if (dependency != classLoader) {
                            try {
                                clazz = dependency.loadClass0(className, false);
                                classMap.put(className, clazz.getClassLoader());
                                break;
                            } catch (ClassNotFoundException e) { }
                        }
                    }
                }

                if (clazz == null) {
                    clazz = this.findClass(className);
                }
            }
        }

        return clazz;
    }

    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        synchronized (this.getClassLoadingLock(className)) {

            Class clazz = loadClass0(className, true);
            if (resolve) {
                this.resolveClass(clazz);
            }

            return clazz;
        }
    }

    /**
     * Add compiled byte code of the class directly to this class loader
     * @param className The name of the class to define
     * @param classByteCode Compiled byte code of the class to add
     * @return The reference to the class
     */
    public Class<?> addClass(final String className, byte[] classByteCode) {
        return defineClass(className, classByteCode, 0, classByteCode.length);
    }

    public ClassLoader getCompilationClassLoader() { return this; }

    /**
     * Add new instance of {@link URL} to the current url class loader if url class loader doesn't contain this instance of {@link URL}
     * @param url instance of {@link URL}
     */
    public void addURL(final URL url) {
        super.addURL(url);
    }
}