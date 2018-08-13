package info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class for compile string with java code to byte code
 * in memory
 *
 * @since 1.8
 */
class JavacToMemoryCodeCompiler {

    /**
     * System java compiler
     */
    private static JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

    /**
     * Constructor.
     * Creates instance of {@link JavacToMemoryCodeCompiler}
     */
    private JavacToMemoryCodeCompiler() {}

    /**
     * Compile {@link String} with custom class to java byte code and represent
     * compiled class
     * @param className full name of future class
     * @param classSourceCode code source
     * @param classLoader instance of {@link ClassLoader} to put compiled code to
     * @return compiled class
     * @throws Exception if any errors occurred
     */
    synchronized static Class<?> compile(
            final String className,
            final String classSourceCode,
            final ClassLoader classLoader
    )
            throws Exception {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) { }
        try {
            List<String> optionList = new ArrayList<>();
            if (null != classLoader) {
                optionList.addAll(Arrays.asList("-classpath", getClassPath(classLoader)));
            }
            SourceCode sourceCode = new SourceCode(className, classSourceCode);
            CompiledCode compiledCode = new CompiledCode(className);
            List compilationUnits = Collections.singletonList(sourceCode);
            ExtendedJavaFileManager fileManager = new ExtendedJavaFileManager(
                    javac.getStandardFileManager(null, null, null),
                    compiledCode,
                    classLoader
            );
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            CompilationTask task = javac.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    optionList,
                    null,
                    compilationUnits
            );
            if (!task.call()) {
                StringBuilder s = new StringBuilder();
                for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                    s
                            .append("\n")
                            .append(diagnostic);
                }
                throw new Exception("Failed to compile " + className + s.toString());

            }
            return ((ExpansibleURLClassLoader)classLoader).addClass(
                    compiledCode.getName(),
                    compiledCode.getByteCode()
            );
        } catch (Throwable e) {
            throw new Exception(e);
        }
    }

    /**
     * Append all class paths to given {@link StringBuilder} form given {@link ClassLoader}
     * recursively with given separator
     * @param buf instance of {@link StringBuilder}
     * @param classLoader instance of {@link ClassLoader}
     * @param separator instance of {@link String}
     */
    private static void addClassPathRecursively(StringBuilder buf, final ClassLoader classLoader, String separator) {
        try {
            ExpansibleURLClassLoader cl = (ExpansibleURLClassLoader) classLoader;

            URL[] urls = cl.getURLs();
            for (URL url : urls) {
                String jarPathName = url.getFile();
                if (jarPathName.startsWith("file:")) {
                    jarPathName = jarPathName.substring(
                            jarPathName.indexOf("file:") + "file:".length(), jarPathName.indexOf("!/")
                    );
                }
                buf
                        .append(separator)
                        .append(jarPathName);
            }
            for(ClassLoader dependency : cl.getDependencies()) {
                addClassPathRecursively(buf, dependency, separator);
            }

        } catch (Exception e) {
            // do nothing
            // because this try-catch check cast ClassLoader to ExpansibleURLClassLoader
        }
    }

    /**
     * Return all class paths as instance of {@link String} form given instance of {@link ClassLoader} recursively
     * @param classLoader instance of {@link ClassLoader}
     * @return all class paths taken from {@link ClassLoader} and its dependencies
     */
    private static String getClassPath(final ClassLoader classLoader) {
        StringBuilder buf = new StringBuilder();
        buf.append(".");
        addClassPathRecursively(buf, classLoader, System.getProperty("path.separator"));
        return buf.toString();
    }
}