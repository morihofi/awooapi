package net.fuxle.awooapi.core.autodiscovery.loader;

import net.fuxle.awooapi.annotations.MultiEndpoint;
import net.fuxle.awooapi.core.templates.AbstractEndpoint;
import net.fuxle.awooapi.core.exceptions.AwooApiException;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.Set;

public class LoaderHelper {

    /**
     * Validates an API path to ensure it meets certain requirements.
     *
     * @param path The API path to be validated.
     * @throws AwooApiException If the API path is invalid.
     */
    public static void validatePath(String path) throws AwooApiException {
        if (path.isEmpty()) {
            throw new AwooApiException("API Path cannot be empty");
        }
        if (!path.startsWith("/")) {
            throw new AwooApiException("API Path must start with a \"/\"");
        }
    }

    /**
     * Constructs a full API path by combining the path prefix, API version, and the specific path.
     *
     * @param pathPrefix The prefix for API paths.
     * @param apiVersion The API version to be included in the path (can be empty).
     * @param path       The specific path for the plugin.
     * @return The constructed full API path.
     */
    public static String constructPath(String pathPrefix, String apiVersion, String path) {
        String versionPrefix = apiVersion.isEmpty() ? "" : "/" + apiVersion;
        return pathPrefix + versionPrefix + path;
    }

    @SuppressWarnings("unchecked")
    public static Set<Class<? extends AbstractEndpoint<?>>> getEndpointClasses(String basePackage, Class<?> expectedBaseClass, Reflections reflections) {
        // Reflections-Instanz für das angegebene Paket erstellen


        // Alle Klassen mit @MultiEndpoint finden
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(MultiEndpoint.class);

        // Ergebnis-Set für gültige Klassen
        Set<Class<? extends AbstractEndpoint<?>>> validClasses = new HashSet<>();

        // Überprüfen, ob jede Klasse die erwartete Basisklasse erweitert
        for (Class<?> clazz : annotatedClasses) {
            if (!expectedBaseClass.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(
                        "Class " + clazz.getName() + " is annotated with @" + MultiEndpoint.class.getSimpleName()  +"  but does not extend " + expectedBaseClass.getName()
                );
            }

            // Klasse ist gültig, zum Ergebnis hinzufügen
            validClasses.add((Class<? extends AbstractEndpoint<?>>) clazz);
        }

        return validClasses;
    }
}
