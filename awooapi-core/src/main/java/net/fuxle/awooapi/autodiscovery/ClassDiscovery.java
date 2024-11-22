package net.fuxle.awooapi.autodiscovery;


import net.fuxle.awooapi.RuntimeConfiguration;
import net.fuxle.awooapi.autodiscovery.loader.GraphQLEndpointLoader;
import net.fuxle.awooapi.autodiscovery.loader.MetadataLoader;
import net.fuxle.awooapi.autodiscovery.loader.RestEndpointLoader;
import net.fuxle.awooapi.exceptions.AwooApiException;
import net.fuxle.awooapi.server.intf.WebServer;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;

/**
 * A utility class for loading and registering plugins in the application.
 * This class provides methods to scan the classpath for plugin classes and register them as REST or WebSocket.
 */
public class ClassDiscovery {

    // Set by constructor
    private final RuntimeConfiguration config;
    private final WebServer webServer;

    private final GraphQLEndpointLoader graphQLEndpointLoader = new GraphQLEndpointLoader(this);
    private final RestEndpointLoader restEndpointLoader = new RestEndpointLoader(this);
    private final MetadataLoader metadataLoader = new MetadataLoader(this);

    /**
     * The logger instance for logging plugin loading and registration messages.
     */
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public ClassDiscovery(RuntimeConfiguration config, WebServer webServer) {
        this.config = config;
        this.webServer = webServer;

    }

    /**
     * Loads and registers REST, WebSocket, and GraphQL plugins based on annotations and configuration.
     *
     * @throws Exception               If an error occurs during plugin loading.
     */
    public void discoverAndLoadClasses() throws Exception {

        log.info("\uD83D\uDD0E Scanning in {} for component classes ...", config.getSearchConfig().getPackagePrefix() + ".*");

        if(config.getApiConfig().isRestEnabled()){
            // REST Endpoints
            restEndpointLoader.loadAndRegisterRestEndpoints(List.of(), webServer);
        }

        if (config.getApiConfig().isGraphQLEnabled()) {
            // GraphQL
            graphQLEndpointLoader.scanForGraphQl();
            graphQLEndpointLoader.registerGraphQlEndpoint(webServer);
        }
    }

    private void loadAndRegisterRESTandWebSocketPlugins(String pathPrefix, Reflections reflections, WebServer webServer) {
        // Finde alle Klassen mit passenden Annotationen
        // Set<Class<?>> WebSocketEndpointClasses = reflections.getTypesAnnotatedWith(WebSocketEndpoint.class);


        // Durchlaufe alle gefundenen Klassen
        /*
        for (Class<?> clazz : WebSocketEndpointClasses) {

            // Finde den passenden Konstruktor
            Constructor<?> constructor = clazz.getDeclaredConstructor();

            // Erstelle eine neue Instanz der Klasse mit den gegebenen Parametern (Constructor wird auto. aufgerufen)
            WebSocketEndpointTemplate instance = (WebSocketEndpointTemplate) constructor.newInstance();

            String path = clazz.getAnnotation(WebSocketEndpoint.class).path();
            String[] apiVersions = clazz.getAnnotation(WebSocketEndpoint.class).apiVersion();
            boolean debugOnly = clazz.getAnnotation(WebSocketEndpoint.class).debugOnly();

            if (debugOnly && !config.isDebugEnabled()) {
                //Production mode, don't enable plugins that should only run in debug mode
                continue;
            }

            validatePath(path);

            for (String apiVersion : apiVersions) {
                path = constructPath(pathPrefix, apiVersion, path);

                javalin.ws(path, wsConfig -> {
                    wsConfig.onConnect(wsConnectContext -> {
                        instance.onConnect(wsConnectContext);
                    });
                    wsConfig.onClose(wsCloseContext -> instance.onClose(wsCloseContext));
                    wsConfig.onError(wsErrorContext -> instance.onError(wsErrorContext));
                    wsConfig.onMessage(wsMessageContext -> instance.onMessage(wsMessageContext));
                    wsConfig.onBinaryMessage(wsBinaryMessageContext -> instance.onBinaryMessage(wsBinaryMessageContext));
                });
                log.info("\u2194\uFE0F Websocket-Plugin class " + clazz.getName() + " loaded, listening at " + path);

            }

        }
*/
    }


    public Reflections createReflections() {
        // Erstelle ein Reflections Objekt, um den root Classpath zu scannen
        Configuration configuration = new ConfigurationBuilder()
                .forPackages(config.getSearchConfig().getPackagePrefix())
                .addClassLoaders(config.getSearchConfig().getClassLoader());

        return new Reflections(configuration);
    }

    public RuntimeConfiguration getConfig() {
        return config;
    }

    public void dumpGraphQlSchema(Path targetLocation) throws IOException {
        graphQLEndpointLoader.saveMergedSchemaToFile(targetLocation);
    }
}
