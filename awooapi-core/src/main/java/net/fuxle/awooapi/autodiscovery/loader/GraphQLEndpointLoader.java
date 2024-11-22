package net.fuxle.awooapi.autodiscovery.loader;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import net.fuxle.awooapi.annotations.GraphQlFieldType;
import net.fuxle.awooapi.annotations.HandlerType;
import net.fuxle.awooapi.annotations.MultiEndpoint;
import net.fuxle.awooapi.autodiscovery.ClassDiscovery;
import net.fuxle.awooapi.autodiscovery.GraphQLEndpoint;
import net.fuxle.awooapi.autodiscovery.dispatcher.GraphQLDispatcher;
import net.fuxle.awooapi.core.templates.AbstractEndpoint;
import net.fuxle.awooapi.server.intf.Endpoint;
import net.fuxle.awooapi.server.intf.WebServer;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
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


public class GraphQLEndpointLoader {
    private final ClassDiscovery classDiscovery;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private GraphQLSchema graphQLSchema = null;

    public GraphQLEndpointLoader(ClassDiscovery classDiscovery) {
        this.classDiscovery = classDiscovery;
    }

    public void scanForGraphQl() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Set<Class<?>> apiPluginMultiClasses = classDiscovery.createReflections().getTypesAnnotatedWith(MultiEndpoint.class);

        Map<String, DataFetcher<?>> queryDataFetchers = new HashMap<>();
        Map<String, DataFetcher<?>> mutationDataFetchers = new HashMap<>();


        for (Class<?> clazz : apiPluginMultiClasses) {
            String fieldName = clazz.getAnnotation(MultiEndpoint.class).graphQLFieldName();
            GraphQlFieldType fieldType = clazz.getAnnotation(MultiEndpoint.class).graphQLFieldType();


            // Finde den passenden Konstruktor
            Constructor<?> constructor = clazz.getDeclaredConstructor();

            // Erstelle eine neue Instanz der Klasse mit den gegebenen Parametern (Constructor wird auto. aufgerufen)
            AbstractEndpoint<?> instance = (AbstractEndpoint<?>) constructor.newInstance();


            if (fieldType == GraphQlFieldType.QUERY) {
                queryDataFetchers.put(fieldName, new GraphQLDispatcher<>(instance));
            } else if (fieldType == GraphQlFieldType.MUTATION) {
                mutationDataFetchers.put(fieldName, new GraphQLDispatcher<>(instance));
            }

            log.info("🔌 Multi-Plugin class {} registered on {} GraphQL query field \"{}\"", clazz.getName(), fieldType.name(), fieldName);
        }

        RuntimeWiring graphQLWiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> {
                    for (Map.Entry<String, DataFetcher<?>> entry : queryDataFetchers.entrySet()) {
                        typeWiring.dataFetcher(entry.getKey(), entry.getValue());
                    }
                    return typeWiring;
                })
                .type("Mutation", typeWiring -> {
                    for (Map.Entry<String, DataFetcher<?>> entry : mutationDataFetchers.entrySet()) {
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
            String schema = new Scanner(Objects.requireNonNull(ClassDiscovery.class.getResourceAsStream("/" + graphQLFile)), StandardCharsets.UTF_8).useDelimiter("\\A").next();
            TypeDefinitionRegistry parsedSchema = new SchemaParser().parse(schema);

            //Add to merged schemas
            mergedRegistry.merge(parsedSchema);

            log.info("\uD83D\uDCC3 GraphQL schema at \"{}\" merged", graphQLFile);
        }

        // Generate the GraphQLSchema from the merged TypeDefinitionRegistry and RuntimeWiring

        this.graphQLSchema = new SchemaGenerator().makeExecutableSchema(mergedRegistry, graphQLWiring);
    }

    public void registerGraphQlEndpoint(WebServer webServer){
        if(graphQLSchema == null){
            throw new IllegalArgumentException("Schema is null, please scan for GraphQL classes first");
        }

        log.info("\u27A1\uFE0F Register GraphQL on {}/graphql", classDiscovery.getConfig().getApiConfig().getApiPrefix());
        webServer.getRouter().addHandler(new Endpoint(HandlerType.POST, classDiscovery.getConfig().getApiConfig().getApiPrefix() + "/graphql", new GraphQLEndpoint(graphQLSchema)));
    }

    protected String parseClassName(String metadata) {
        String[] lines = metadata.split("\n");
        for (String line : lines) {
            if (line.startsWith("class=")) {
                return line.split("=")[1].trim();
            }
        }
        throw new IllegalArgumentException("Class name not found in metadata");
    }

    public void saveMergedSchemaToFile(Path target) throws IOException {
        saveMergedSchemaToFile(graphQLSchema, target);
    }

    public void saveMergedSchemaToFile(GraphQLSchema graphQLSchema, Path target) throws IOException {
        String schemaString = new SchemaPrinter().print(graphQLSchema);
        Files.writeString(target, """
                # This file is the autogenerated GraphQL schema and MUST NOT be checked into version control.
                # It is used to be able to compile GraphQL client application (e.g., Frontends) while the Backend is offline, e.g., in Pipelines
                #
                """ + schemaString);
        log.info("\uD83D\uDCBE Dumped merged schema to {} for offline frontend type generation", target);
    }

    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }
}
