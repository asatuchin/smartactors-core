package info.smart_tools.smartactors.core.plugin_loader_from_jar;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin_loader.IPluginLoader;
import info.smart_tools.smartactors.core.iplugin_loader.exception.PluginLoaderException;
import info.smart_tools.smartactors.core.iplugin_loader_visitor.IPluginLoaderVisitor;

import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Implementation of {@link info.smart_tools.smartactors.core.iplugin_loader.IPluginLoader}.
 * <pre>
 * Main features of implementation:
 * - find implementation of {@link info.smart_tools.smartactors.core.iplugin.IPlugin} in given Jar file.
 * - implementation of {@link info.smart_tools.smartactors.core.iplugin.IPlugin} should be has constructor with
 * given parameters.
 * </pre>
 */
public class PluginLoader implements IPluginLoader<String> {

    private static final String CLASS_EXTENSION = ".class";

    /** ClassLoader for load classes*/
    private ExpansibleURLClassLoader classLoader;

    /** Action to create instance of given class */
    private IAction<Class> creator;

    /** Visitor contains some handlers to handle success or fail loading execution */
    private IPluginLoaderVisitor<String> visitor;

    /**
     * Constructor with two arguments
     * @param classLoader instance of {@link ClassLoader}
     * @param action instance of {@link IAction}
     * @param visitor instance of {@link IPluginLoaderVisitor}
     * @throws InvalidArgumentException if incoming argument are wrong
     */
    public PluginLoader(final ClassLoader classLoader, final IAction<Class> action, final IPluginLoaderVisitor<String> visitor)
            throws InvalidArgumentException {
        if (null == action || null == classLoader || null == visitor) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.creator = action;
        this.visitor = visitor;
        try {
            this.classLoader = (ExpansibleURLClassLoader) classLoader;
        } catch (Throwable e) {
            throw new InvalidArgumentException("Could not cast given ClassLoader to the URLClassLoader.");
        }
    }

    @Override
    public void loadPlugin(final String pathToJar)
            throws PluginLoaderException {
        try {
            JarFile jarFile = new JarFile(pathToJar);
            Enumeration<JarEntry> iterator = jarFile.entries();
            URL url = new URL("jar:file:" + pathToJar + "!/");
            this.classLoader.addUrl(url);

            while (iterator.hasMoreElements()) {
                JarEntry je = iterator.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(CLASS_EXTENSION)) {
                    continue;
                }
                String className = je.getName().substring(0, je.getName().length() - CLASS_EXTENSION.length());
                className = className.replace('/', '.');
                Class clazz = classLoader.loadClass(className);
                if (Arrays.asList(clazz.getInterfaces()).contains(IPlugin.class)) {
                    creator.execute(clazz);
                }
            }

        } catch (Throwable e) {
            visitor.pluginLoadingFail(pathToJar, e);
            throw new PluginLoaderException("Plugin loading failed.", e);
        }
    }
}
