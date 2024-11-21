package net.fuxle.awooapi.autodiscovery.abstracttemplates;


import net.fuxle.awooapi.autodiscovery.utility.Parameters;

/**
This class is used to implement your own REST API Endpoint.
 */
public abstract class RESTEndpointTemplate<T> {
    public abstract T handleRequest(Parameters params) throws Exception;
}
