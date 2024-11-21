package net.fuxle.awooapi.autodiscovery.abstracttemplates;

/**
 * Template for an Endpoint, that supports REST and GraphQL queries/mutations out of the same code
 * @param <T> Return Type, must be specified in GraphQL Schema
 */
public abstract class MultiEndpointTemplate<T> extends RESTEndpointTemplate {

}
