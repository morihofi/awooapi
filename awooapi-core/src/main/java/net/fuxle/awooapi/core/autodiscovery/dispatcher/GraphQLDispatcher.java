package net.fuxle.awooapi.core.autodiscovery.dispatcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import net.fuxle.awooapi.core.autodiscovery.GraphQLLocalContext;
import net.fuxle.awooapi.core.templates.AbstractEndpoint;
import net.fuxle.awooapi.core.api.Parameters;

/**
 * A data fetcher implementation for GraphQL that serves as a dispatcher to handle GraphQL queries.
 * It uses an instance of {@link AbstractEndpoint} to process GraphQL queries and return results.
 *
 * @param <T> The type of data to be fetched and returned by this dispatcher.
 */
public class GraphQLDispatcher<T> implements DataFetcher<T> {
    /**
     * The instance of {@link AbstractEndpoint} used to process GraphQL queries.
     */
    private final AbstractEndpoint<T> multiEndpointInstance;

    /**
     * Constructs a new GraphQLDispatcher with the provided instance of {@link AbstractEndpoint}.
     *
     * @param multiEndpointInstance The instance of {@link AbstractEndpoint} to be used for processing queries.
     */
    public GraphQLDispatcher(AbstractEndpoint<T> multiEndpointInstance) {
        this.multiEndpointInstance = multiEndpointInstance;
    }

    /**
     * Retrieves data based on the provided GraphQL query and environment.
     *
     * @param environment The GraphQL data fetching environment.
     * @return The fetched data of type {@code T}.
     * @throws Exception If an error occurs during data fetching.
     */
    @Override
    public T get(DataFetchingEnvironment environment) throws Exception {
        // Retrieve the GraphQLLocalContext set in GraphQLEndpoint class
        GraphQLLocalContext graphQLLocalContext = environment.getLocalContext();

        if (graphQLLocalContext == null) {
            throw new IllegalArgumentException("GraphQL Local Context is null, but it cannot be. Something must be really wrong here");
        }

        // Create Parameters for processing the query
        Parameters params = new Parameters(
                graphQLLocalContext.handlerContext(), // Handler Context
                environment, // GraphQL Environment
                Parameters.REQUEST_SOURCE.GRAPH_QL // Request Source
        );

        // Run the multiEndpointInstance to process the GraphQL query and return the result
        return multiEndpointInstance.handleRequest(params);
    }
}
