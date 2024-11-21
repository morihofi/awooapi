package net.fuxle.awooapi.autodiscovery;


import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import net.fuxle.awooapi.RuntimeConfiguration;
import net.fuxle.awooapi.annotations.*;
import net.fuxle.awooapi.autodiscovery.dispatcher.GraphQLDispatcher;
import net.fuxle.awooapi.autodiscovery.dispatcher.RESTDispatcher;
import net.fuxle.awooapi.autodiscovery.abstracttemplates.AutoloadClassTemplate;
import net.fuxle.awooapi.autodiscovery.abstracttemplates.MultiEndpointTemplate;
import net.fuxle.awooapi.autodiscovery.abstracttemplates.RESTEndpointTemplate;
import net.fuxle.awooapi.server.intf.Endpoint;
import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.WebServer;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A utility class for loading and registering plugins in the application.
 * This class provides methods to scan the classpath for plugin classes and register them as REST or WebSocket.
 */
public class EndpointClassDiscovery {

    private final RuntimeConfiguration config;

    /**
     * The logger instance for logging plugin loading and registration messages.
     */
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ClassLoader callingClassLoader;

    public EndpointClassDiscovery(RuntimeConfiguration config, ClassLoader callingClassLoader) {
        this.config = config;
        this.callingClassLoader = callingClassLoader;
    }


    /**
     * Loads and registers REST, WebSocket, and GraphQL plugins based on annotations and configuration.
     *
     * @param webServer    The WebServer instance to which plugins will be registered.
     * @param pathPrefix The prefix for API paths where plugins will be registered.
     * @throws NoSuchMethodException     If a plugin class lacks a required constructor.
     * @throws InvocationTargetException If an error occurs while invoking a plugin's constructor.
     * @throws InstantiationException    If there is an issue with instantiating a plugin.
     * @throws IllegalAccessException    If there is illegal access to a plugin class or constructor.
     * @throws IOException               If an IO error occurs during plugin loading.
     * @throws ClassDiscoveryException   If there is an issue with loading a plugin.
     */
    public void discoverAndLoadClasses(WebServer webServer, String pathPrefix) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, ClassDiscoveryException {

        log.info("\uD83D\uDD0E Scanning in \"" + config.getSearchPackagePrefix() + ".*\" for component classes ...");

        // Erstelle ein Reflections Objekt, um den root Classpath zu scannen
        Configuration configuration = new ConfigurationBuilder()
                .forPackages(config.getSearchPackagePrefix())
                .addClassLoaders(callingClassLoader);

        Reflections reflections = new Reflections(configuration);

        loadAndRegisterRESTandWebSocketPlugins(pathPrefix, reflections, webServer);
        loadAndRegisterGraphQLPlugins(pathPrefix, reflections, webServer, null);

    }

    public void dumpGqlSchema(Path targetDumpLocation) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        log.info("\uD83D\uDD0E Scanning in \"{}.*\" for component classes ...", config.getSearchPackagePrefix());

        // Erstelle ein Reflections Objekt, um den root Classpath zu scannen
        Reflections reflections = new Reflections(config.getSearchPackagePrefix());

        loadAndRegisterGraphQLPlugins("", reflections, null, targetDumpLocation);
    }

    /**
     * Validates an API path to ensure it meets certain requirements.
     *
     * @param path The API path to be validated.
     * @throws ClassDiscoveryException If the API path is invalid.
     */
    private static void validatePath(String path) throws ClassDiscoveryException {
        if (path.isEmpty()) {
            throw new ClassDiscoveryException("API Path cannot be empty");
        }
        if (!path.startsWith("/")) {
            throw new ClassDiscoveryException("API Path must start with a \"/\"");
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
    private static String constructPath(String pathPrefix, String apiVersion, String path) {
        String versionPrefix = apiVersion.isEmpty() ? "" : "/" + apiVersion;
        return pathPrefix + versionPrefix + path;
    }

    static void loadAndRegisterGraphQLPlugins(String pathPrefix, Reflections reflections, WebServer webServer, Path targetLocation) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {

        Set<Class<?>> apiPluginEndpointClasses = reflections.getTypesAnnotatedWith(GraphQLQuery.class);
        Set<Class<?>> apiPluginMultiClasses = reflections.getTypesAnnotatedWith(MultiEndpoint.class);

        Map<String, DataFetcher> queryDataFetchers = new HashMap<>();
        Map<String, DataFetcher> mutationDataFetchers = new HashMap<>();

        for (Class<?> clazz : apiPluginEndpointClasses) {
            String fieldName = clazz.getAnnotation(GraphQLQuery.class).fieldName();
            GraphQlFieldType fieldType = clazz.getAnnotation(GraphQLQuery.class).graphQLFieldType();


            // Finde den passenden Konstruktor
            Constructor<?> constructor = clazz.getDeclaredConstructor();

            // Erstelle eine neue Instanz der Klasse mit den gegebenen Parametern (Constructor wird auto. aufgerufen)
            DataFetcher<?> instance = (DataFetcher<?>) constructor.newInstance();

            if (fieldType == GraphQlFieldType.QUERY) {
                queryDataFetchers.put(fieldName, instance);
            } else if (fieldType == GraphQlFieldType.MUTATION) {
                mutationDataFetchers.put(fieldName, instance);
            }

            log.info("\uD83D\uDD0C GraphQL DataFetcher-Plugin (" + fieldType.name() + ") class " + clazz.getName() + " registered on field \"" + fieldName + "\"");
        }

        for (Class<?> clazz : apiPluginMultiClasses) {
            String fieldName = clazz.getAnnotation(MultiEndpoint.class).graphQLFieldName();
            GraphQlFieldType fieldType = clazz.getAnnotation(MultiEndpoint.class).graphQLFieldType();


            // Finde den passenden Konstruktor
            Constructor<?> constructor = clazz.getDeclaredConstructor();

            // Erstelle eine neue Instanz der Klasse mit den gegebenen Parametern (Constructor wird auto. aufgerufen)
            MultiEndpointTemplate<?> instance = (MultiEndpointTemplate<?>) constructor.newInstance();


            if (fieldType == GraphQlFieldType.QUERY) {
                queryDataFetchers.put(fieldName, new GraphQLDispatcher(instance));
            } else if (fieldType == GraphQlFieldType.MUTATION) {
                mutationDataFetchers.put(fieldName, new GraphQLDispatcher(instance));
            }


            log.info("\uD83D\uDD0C Multi-Plugin class " + clazz.getName() + " registered on " + fieldType.name() + " GraphQL query field \"" + fieldName + "\"");
        }


        RuntimeWiring graphQLWiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> {
                    for (Map.Entry<String, DataFetcher> entry : queryDataFetchers.entrySet()) {
                        typeWiring.dataFetcher(entry.getKey(), entry.getValue());
                    }
                    return typeWiring;
                })
                .type("Mutation", typeWiring -> {
                    for (Map.Entry<String, DataFetcher> entry : mutationDataFetchers.entrySet()) {
                        typeWiring.dataFetcher(entry.getKey(), entry.getValue());
                    }
                    return typeWiring;
                })
                .build();

        log.info("\uD83D\uDD0E Scanning classpath for GraphQL (*.graphql) schemas...");

        // Erstellen Sie ein Reflections-Objekt mit einem ResourcesScanner
        Reflections reflectionsScanner = new Reflections("graphql", Scanners.Resources);
        // Suchen Sie nach allen Dateien, die mit .graphql enden
        Set<String> graphQLFiles = reflectionsScanner.getResources(Pattern.compile(".*\\.graphql"));

        TypeDefinitionRegistry mergedRegistry = new TypeDefinitionRegistry();
        for (String graphQLFile : graphQLFiles) {
            String schema = new Scanner(Objects.requireNonNull(EndpointClassDiscovery.class.getResourceAsStream("/" + graphQLFile)), StandardCharsets.UTF_8).useDelimiter("\\A").next();
            TypeDefinitionRegistry parsedSchema = new SchemaParser().parse(schema);

            //Add to merged schemas
            mergedRegistry.merge(parsedSchema);

            log.info("\uD83D\uDCC3 GraphQL schema at \"{}\" merged", graphQLFile);
        }

        if (webServer == null && targetLocation != null) {
            saveMergedSchemaToFile(mergedRegistry, graphQLWiring,
                    targetLocation
            );

            return;
        }


        log.info("\u27A1\uFE0F Register GraphQL on " + pathPrefix + "/graphql");
        webServer.getRouter().addHandler(new Endpoint(HandlerType.POST, pathPrefix + "/graphql", new GraphQLEndpoint(mergedRegistry, graphQLWiring)));

    }

    /**
     * Save the merged schema to a file.
     */
    private static void saveMergedSchemaToFile(TypeDefinitionRegistry mergedRegistry, RuntimeWiring runtimeWiring, Path target) throws IOException {

        // Generate the GraphQLSchema from the merged TypeDefinitionRegistry and RuntimeWiring
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(mergedRegistry, runtimeWiring);

        // Use SchemaPrinter to print the schema to a string
        SchemaPrinter schemaPrinter = new SchemaPrinter();
        String schemaString = schemaPrinter.print(graphQLSchema);

        // Write the schema string to a file
        Files.writeString(target, """
                # This file is the autogenerated GraphQL schema and MUST NOT be checked into version control.
                # It is used to be able to compile GraphQL client application (e.g. Frontends) while the Backend is offline, e.g. in Pipelines
                #
                """ + schemaString);

        log.info("\uD83D\uDCBE Dumped merged schema to {} for offline frontend type generation", target);
    }


    private void loadAndRegisterRESTandWebSocketPlugins(String pathPrefix, Reflections reflections, WebServer webServer) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassDiscoveryException {
        // Finde alle Klassen mit passenden Annotationen
        Set<Class<?>> RESTEndpointClasses = reflections.getTypesAnnotatedWith(RESTEndpoint.class);
        Set<Class<?>> WebSocketEndpointClasses = reflections.getTypesAnnotatedWith(WebSocketEndpoint.class);
        Set<Class<?>> AutoloadClasses = reflections.getTypesAnnotatedWith(AutoloadClass.class);
        Set<Class<?>> MultiEndpointClasses = reflections.getTypesAnnotatedWith(MultiEndpoint.class);


        for (Class<?> clazz : AutoloadClasses) {

            // Finde den passenden Konstruktor
            Constructor<?> constructor = clazz.getDeclaredConstructor();

            boolean debugOnly = clazz.getAnnotation(AutoloadClass.class).debugOnly();

            if (debugOnly && !config.isDebugEnabled()) {
                //Production mode, don't enable plugins that should only run in debug mode
                continue;
            }

            // Erstelle eine neue Instanz der Klasse mit den gegebenen Parametern (Constructor wird auto. aufgerufen)
            AutoloadClassTemplate instance = (AutoloadClassTemplate) constructor.newInstance();

            log.info("\u26A1 Invoking onPluginLoad() method in class {}", clazz.getName());
            instance.onClassLoad();
        }


        // Durchlaufe alle gefundenen Klassen
        for (Class<?> clazz : RESTEndpointClasses) {

            // Finde den passenden Konstruktor
            Constructor<?> constructor = clazz.getDeclaredConstructor();

            // Erstelle eine neue Instanz der Klasse mit den gegebenen Parametern (Constructor wird auto. aufgerufen)

            Object objInstance = constructor.newInstance();
            if (!(objInstance instanceof RESTEndpointTemplate)) {
                throw new ClassDiscoveryException("Annotation is annotated on class with wrong type. Must be " + RESTEndpointTemplate.class.getName() + ", instead of " + objInstance.getClass().getName());
            }
            RESTEndpointTemplate instance = (RESTEndpointTemplate) objInstance;


            HandlerType[] types = clazz.getAnnotation(RESTEndpoint.class).type();
            String path = clazz.getAnnotation(RESTEndpoint.class).path();
            String[] apiVersions = clazz.getAnnotation(RESTEndpoint.class).apiVersion();
            boolean debugOnly = clazz.getAnnotation(RESTEndpoint.class).debugOnly();

            if (debugOnly && !config.isDebugEnabled()) {
                //Production mode, don't enable plugins that should only run in debug mode
                continue;
            }

            validatePath(path);

            for (String apiVersion : apiVersions) {

                path = constructPath(pathPrefix, apiVersion, path);


                for (HandlerType type : types) {
                    Handler handler = new RESTDispatcher(instance, config);
                    webServer.getRouter().addHandler(new Endpoint(type, path, handler));
                    log.info("\uD83D\uDD0C REST-Plugin class " + clazz.getName() + " loaded, listening on " + type + " " + path);
                }
            }

        }


        // Durchlaufe alle gefundenen Klassen
        for (Class<?> clazz : MultiEndpointClasses) {

            // Finde den passenden Konstruktor
            Constructor<?> constructor = clazz.getDeclaredConstructor();

            // Erstelle eine neue Instanz der Klasse mit den gegebenen Parametern (Constructor wird auto. aufgerufen)
            MultiEndpointTemplate multiEndpointInstance = (MultiEndpointTemplate) constructor.newInstance();

            HandlerType[] types = clazz.getAnnotation(MultiEndpoint.class).restType();
            String path = clazz.getAnnotation(MultiEndpoint.class).restPath();
            String[] apiVersions = clazz.getAnnotation(MultiEndpoint.class).restVersionPrefix();
            boolean debugOnly = clazz.getAnnotation(MultiEndpoint.class).debugOnly();

            if (debugOnly && !config.isDebugEnabled()) {
                //Production mode, don't enable plugins that should only run in debug mode
                continue;
            }

            validatePath(path);

            for (String apiVersion : apiVersions) {

                path = constructPath(pathPrefix, apiVersion, path);

                for (HandlerType type : types) {
                    webServer.getRouter().addHandler(new Endpoint(
                                    type,
                                    path,
                                    new RESTDispatcher(multiEndpointInstance, config)
                            )
                    );
                    log.info("\uD83D\uDD0C Multi-Plugin class {} loaded as REST, listening on {} {}", clazz.getName(), type, path);
                }
            }

        }

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


}
