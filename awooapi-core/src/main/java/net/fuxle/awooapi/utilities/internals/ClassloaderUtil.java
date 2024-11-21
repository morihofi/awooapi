package net.fuxle.awooapi.utilities.internals;

public class ClassloaderUtil {
    public static ClassLoader getCallingClassLoader() {
        // Get the stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // The caller is usually at index 3 (depends on method structure)
        String callingClassName = stackTrace[3].getClassName();

        try {
            // Load the class and get its ClassLoader
            Class<?> callingClass = Class.forName(callingClassName);
            return callingClass.getClassLoader();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
