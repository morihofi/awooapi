package net.fuxle.awooapi.utilities.internals.helper;

import java.nio.file.Files;
import java.nio.file.Path;

public class MavenHelper {

    public static Path findMavenProjectRoot(Path currentDir) {

        while (currentDir != null) {
            if (Files.exists(currentDir.resolve("pom.xml"))) {
                return currentDir;
            }
            currentDir = currentDir.getParent();
        }

        return null;
    }

}
