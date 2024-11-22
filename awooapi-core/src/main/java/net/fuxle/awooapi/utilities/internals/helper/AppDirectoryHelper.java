package net.fuxle.awooapi.utilities.internals.helper;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Helper class for retrieving application directory and JAR file path.
 * This class provides methods to obtain the application's directory and JAR file path as Path objects.
 */
public class AppDirectoryHelper {

    /**
     * Gets the application directory where the application is located using the provided class.
     *
     * @param clazz The class used to locate the application's directory.
     * @return The application directory as a Path object, or null if an error occurs.
     */
    public static Path getAppDirectory(Class<?> clazz) {
        try {
            return Paths.get(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * Gets the file path to the application's JAR file using the provided class.
     *
     * @param clazz The class used to locate the JAR file path.
     * @return The JAR file path as a Path object, or null if an error occurs.
     */
    public static Path getAppJarFilePath(Class<?> clazz) {
        try {
            return Paths.get(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * Gets the application directory where the application is located using the ClassLoader.
     *
     * @param classLoader The ClassLoader used to locate the application's directory.
     * @return The application directory as a Path object, or null if an error occurs.
     */
    public static Path getAppDirectoryUsingClassLoader(ClassLoader classLoader) {
        try {
            File file = new File(Objects.requireNonNull(classLoader.getResource("")).toURI());
            return file.toPath();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the file path to the application's JAR file using the ClassLoader.
     *
     * @param classLoader The ClassLoader used to locate the JAR file path.
     * @return The JAR file path as a Path object, or null if an error occurs.
     */
    public static Path getAppJarFilePathUsingClassLoader(ClassLoader classLoader) {
        try {
            File file = new File(Objects.requireNonNull(classLoader.getResource("")).toURI());
            return file.toPath();
        } catch (Exception e) {
            return null;
        }
    }

    private AppDirectoryHelper() {}
}
