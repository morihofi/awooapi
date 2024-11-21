package net.fuxle.awooapi.utilities.internals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


/**
 * Utility class for dynamically loading external JAR files at runtime.
 * This approach is useful for dependencies that cannot be included in the source code
 * or are only available in binary form we can't publish for the public.
 * By using this method, JARs can be loaded conditionally based on their availability at runtime.
 *
 * <p>This mechanism ensures that the application remains functional and compilable
 * even when certain optional dependencies are not present. If a dependency is found
 * at runtime, it is loaded dynamically to enable specific functionality. If the
 * dependency is unavailable, the corresponding features are gracefully disabled,
 * while the rest of the application continues to operate as expected.</p>
 *
 * <p>For example, this approach can be employed to load optional components or other modular features
 * that are not critical to the core application functionality.</p>
 *
 * <p>Note: Proper error handling and fallback mechanisms should be implemented to
 * handle cases where the JARs are missing or incompatible.</p>
 *
 * @author Moritz Hofmann
 */

public class JarPluginLoader {

    private final Map<String, ClassLoader> registeredClasses;
    private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Private constructor to initialize the JarPluginLoader with a map of registered classes.
     *
     * @param registeredClasses a map of class names to their corresponding class loaders.
     */
    private JarPluginLoader(Map<String, ClassLoader> registeredClasses) {
        this.registeredClasses = registeredClasses;
    }

    /**
     * Static method to load JAR files from the plugin directory and register their classes.
     *
     * @return an instance of JarPluginLoader containing registered classes.
     */
    public static JarPluginLoader getPluginLoader(Path pluginsDir) {
        Map<String, ClassLoader> registeredClasses = new HashMap<>();

        log.info("Trying to load external jars ... Please wait");
        try {
            validateDirectory(pluginsDir);

            List<URL> jarUrls = getJarUrls(pluginsDir);
            if (jarUrls.isEmpty()) {
                log.info("No JAR files found in the plugins directory: {}. Skipping plugin registration", pluginsDir.toAbsolutePath());
                return new JarPluginLoader(registeredClasses);
            }

            URLClassLoader classLoader = new URLClassLoader(jarUrls.toArray(new URL[0]), JarPluginLoader.class.getClassLoader());
            loadClassesFromJars(jarUrls, classLoader, registeredClasses);
        } catch (Exception e) {
            log.warn("Exception occurred while loading jars", e);
        }

        return new JarPluginLoader(registeredClasses);
    }

    /**
     * Validates the given directory to ensure it exists and is a directory.
     *
     * @param pluginsDir the path to the plugins directory.
     */
    private static void validateDirectory(Path pluginsDir) {
        if (!Files.exists(pluginsDir) || !Files.isDirectory(pluginsDir)) {
            throw new IllegalStateException("Plugins directory not found or is not a directory: " + pluginsDir.toAbsolutePath());
        }
    }

    /**
     * Retrieves a list of JAR file URLs from the specified directory.
     *
     * @param pluginsDir the path to the plugins directory.
     * @return a list of URLs representing the JAR files.
     * @throws IOException if an I/O error occurs while accessing the directory.
     */
    private static List<URL> getJarUrls(Path pluginsDir) throws IOException {
        return Files.list(pluginsDir)
                .filter(path -> path.toString().endsWith(".jar"))
                .map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (Exception e) {
                        log.warn("Could not convert JAR file path to URL: " + path, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Loads classes from the provided JAR files and registers them.
     *
     * @param jarUrls           the list of JAR file URLs.
     * @param classLoader       the URLClassLoader to load classes.
     * @param registeredClasses the map to store registered classes.
     */
    private static void loadClassesFromJars(List<URL> jarUrls, URLClassLoader classLoader, Map<String, ClassLoader> registeredClasses) {
        for (URL jarUrl : jarUrls) {
            try (JarInputStream jarInputStream = new JarInputStream(jarUrl.openStream())) {
                processJarEntries(jarInputStream, classLoader, registeredClasses, jarUrl);
            } catch (IOException e) {
                log.warn("Failed to process JAR file: " + jarUrl, e);
            }
        }
    }

    /**
     * Processes the entries of a JAR file and registers valid classes.
     *
     * @param jarInputStream    the input stream of the JAR file.
     * @param classLoader       the URLClassLoader to load classes.
     * @param registeredClasses the map to store registered classes.
     * @param jarUrl            the URL of the JAR file.
     * @throws IOException if an I/O error occurs while reading the JAR file.
     */
    private static void processJarEntries(JarInputStream jarInputStream, URLClassLoader classLoader, Map<String, ClassLoader> registeredClasses, URL jarUrl) throws IOException {
        JarEntry entry;
        while ((entry = jarInputStream.getNextJarEntry()) != null) {
            if (entry.getName().endsWith(".class")) {
                String className = entry.getName().replace("/", ".").substring(0, entry.getName().length() - 6);
                registeredClasses.put(className, classLoader);
                log.info("Registered class (from {}) : {}", jarUrl.getFile(), className);
            }
        }
    }

    /**
     * Returns an unmodifiable view of the registered plugin classes.
     *
     * @return a map of registered class names to their class loaders.
     */
    public Map<String, ClassLoader> getRegisteredPluginClasses() {
        return Collections.unmodifiableMap(registeredClasses);
    }

    /**
     * Instantiates a new class instance for the specified class name.
     *
     * @param packageDotClassName the fully qualified name of the class.
     * @return the Class object of the specified class.
     * @throws ClassNotFoundException if the class is not found or not registered.
     */
    public Class<?> getNewInitializedClassInstance(String packageDotClassName) throws ClassNotFoundException {
        ClassLoader classLoader = registeredClasses.get(packageDotClassName);
        if (classLoader == null) {
            throw new ClassNotFoundException("Class not found or not registered: " + packageDotClassName);
        }
        return Class.forName(packageDotClassName, true, classLoader);
    }
}
