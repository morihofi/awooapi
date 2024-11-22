package net.fuxle.awooapi.core.templates;

import net.fuxle.awooapi.core.api.Parameters;

/**
 * Template for an Endpoint, that supports REST and GraphQL queries/mutations out of the same code
 * @param <T> Return Type, must be specified in GraphQL Schema
 */
public abstract class AbstractEndpoint<T> {

    public abstract T handleRequest(Parameters params) throws Exception;

}
