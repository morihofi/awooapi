package net.fuxle.awooapi.server.intf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.util.Objects;

public class StaticFileServing {

    public StaticFileServing(STORAGE_LOCATION location, String path, ClassLoader classLoader) {
        this.location = location;
        this.path = path;
        this.classLoader = classLoader;
    }

    private STORAGE_LOCATION location;
    private String path;
    private ClassLoader classLoader;

    public boolean existsFileOrDirectory(String relativePath) {
        Path resolvedPath = resolvePath(relativePath);
        if (location == STORAGE_LOCATION.FILESYSTEM) {
            return Files.exists(resolvedPath);
        } else if (location == STORAGE_LOCATION.CLASSPATH) {
            InputStream resourceStream = classLoader.getResourceAsStream(path + "/" + removeStartingSlashFromPath(relativePath));
            return resourceStream != null;
        }
        return false;
    }

    private static String removeStartingSlashFromPath(String path){
        if(path.startsWith("/")){
            return path.substring(1);
        }
        return path;
    }


    public PATH_TYPE getPathType(String relativePath) {
        Path resolvedPath = resolvePath(relativePath);
        if (location == STORAGE_LOCATION.FILESYSTEM) {
            if (Files.isDirectory(resolvedPath)) {
                return PATH_TYPE.DIRECTORY;
            } else if (Files.isRegularFile(resolvedPath)) {
                return PATH_TYPE.FILE;
            }
        } else if (location == STORAGE_LOCATION.CLASSPATH) {
            String fullPath = path + "/" + removeStartingSlashFromPath(relativePath);
            if (classLoader.getResource(fullPath + "/") != null) {
                return PATH_TYPE.DIRECTORY;
            } else if (classLoader.getResource(fullPath) != null) {
                return PATH_TYPE.FILE;
            }
        }
        throw new IllegalArgumentException("Path type cannot be determined for: " + relativePath);
    }

    public byte[] getFileContents(String relativePath) {
        Path resolvedPath = resolvePath(relativePath);
        if (location == STORAGE_LOCATION.FILESYSTEM) {
            try {
                return Files.readAllBytes(resolvedPath);
            } catch (IOException e) {
                throw new RuntimeException("Unable to read file contents: " + relativePath, e);
            }
        } else if (location == STORAGE_LOCATION.CLASSPATH) {
            try (InputStream resourceStream = classLoader.getResourceAsStream(path + "/" + removeStartingSlashFromPath(relativePath))) {
                if (resourceStream == null) {
                    throw new RuntimeException("File not found in classpath: " + relativePath);
                }
                return resourceStream.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException("Unable to read file contents from classpath: " + relativePath, e);
            }
        }
        throw new IllegalArgumentException("Cannot read contents of: " + relativePath);
    }

    private Path resolvePath(String relativePath) {
        try {
            Path resolvedPath = Paths.get(path).resolve(relativePath).normalize().toAbsolutePath();
            // if (!resolvedPath.startsWith(Paths.get(path).normalize().toAbsolutePath())) {
            //     throw new SecurityException("Path traversal attempt detected: " + relativePath);
            // }
            return resolvedPath;
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid path: " + relativePath, e);
        }
    }

    public STORAGE_LOCATION getLocation() {
        return location;
    }

    public String getPath() {
        return path;
    }

    public enum STORAGE_LOCATION {
        CLASSPATH, FILESYSTEM
    }

    public enum PATH_TYPE {
        FILE, DIRECTORY
    }
}
