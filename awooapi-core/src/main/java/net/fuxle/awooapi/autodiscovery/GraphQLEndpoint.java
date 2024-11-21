package net.fuxle.awooapi.autodiscovery;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import net.fuxle.awooapi.server.intf.HandlerContext;
import net.fuxle.awooapi.server.intf.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;

/**
 * A Javalin handler class responsible for handling GraphQL queries and mutations.
 * This class uses the provided GraphQL schema and wiring to execute GraphQL queries and return JSON responses.
 *
 * @author Moritz Hofmann
 */
public class GraphQLEndpoint implements Handler {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * The GraphQL instance used to execute GraphQL queries.
     */
    private final GraphQL graphQL;

    /**
     * Constructs a new GraphQLEndpoint with the provided schema and wiring.
     *
     * @param schema The TypeDefinitionRegistry containing the GraphQL schema.
     * @param wiring The RuntimeWiring defining how the schema should be wired.
     */
    public GraphQLEndpoint(TypeDefinitionRegistry schema, RuntimeWiring wiring) {
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(
                schema,
                wiring
        );

        graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    /**
     * A Gson instance for serializing GraphQL execution results to JSON.
     */
    public static final Gson gson = new Gson();

    /**
     * Handles the incoming HTTP request as a GraphQL query and returns the JSON response.
     *
     * @param ctx The Javalin HTTP context for handling the request.
     * @throws Exception If an error occurs during request processing.
     */
    @Override
    public void handle(HandlerContext ctx) throws Exception {
        // Set response content type
        ctx.header("Content-Type", "application/json");
        // Disable caching
        ctx.header("Cache-Control", "no-cache, no-store, must-revalidate");
        ctx.header("Pragma", "no-cache");
        ctx.header("Expires", "0");

        // Parse the request body as a JSON object using Gson
        JsonObject requestBody = gson.fromJson(ctx.body(), JsonObject.class);
        String query = requestBody.get("query").getAsString();

        JsonObject variables = requestBody.has("variables") ? requestBody.getAsJsonObject("variables") : null;
        Map<String, Object> variablesMap = variables == null ? Collections.emptyMap() : gson.fromJson(variables, Map.class);

        // Create a GraphQLLocalContext for handling the GraphQL request
        GraphQLLocalContext graphQLLocalContext = new GraphQLLocalContext(ctx);

        log.debug("Query: {}", query);
        if (variables != null) {
            log.debug("Variables: {}", gson.toJson(variables));
        }

        // Build the ExecutionInput for GraphQL execution
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .localContext(graphQLLocalContext)
                .query(query)
                .variables(variablesMap)
                .build();

        JsonElement responseJSON = gson.toJsonTree(graphQL.execute(executionInput).toSpecification());

        log.debug("Response: {}", gson.toJson(responseJSON));

        log.debug("------");

        // Execute the GraphQL query and return the result as JSON
        ctx.result(gson.toJson(responseJSON));
    }
}
