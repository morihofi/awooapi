package net.fuxle.awooapi.autodiscovery.dispatcher;


import com.google.gson.Gson;
import net.fuxle.awooapi.RuntimeConfiguration;
import net.fuxle.awooapi.autodiscovery.abstracttemplates.RESTEndpointTemplate;
import net.fuxle.awooapi.autodiscovery.utility.Parameters;
import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.HandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class RESTDispatcher implements Handler {


    /**
     * The instance of {@link net.fuxle.awooapi.autodiscovery.abstracttemplates.RESTEndpointTemplate} used to process REST requests.
     */
    private RESTEndpointTemplate restEndpointInstance;
    private RuntimeConfiguration configuration;

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Constructs a new RESTDispatcher with the provided instance of {@link RESTEndpointTemplate}.
     *
     * @param restEndpointInstance The instance of {@link RESTEndpointTemplate} to be used for processing REST requests.
     */
    public RESTDispatcher(RESTEndpointTemplate restEndpointInstance, RuntimeConfiguration configuration) {
        this.restEndpointInstance = restEndpointInstance;
        this.configuration = configuration;
    }

    /**
     * Handles the incoming REST request, processes it using the multiEndpointInstance, and returns the response in JSON format.
     *
     * @param ctx The HTTP context for handling the request.
     * @throws Exception If an error occurs during request processing.
     */
    @Override
    public void handle(HandlerContext ctx) {

        Gson gson = new Gson();

        // Create Parameters for processing the REST request
        Parameters params = new Parameters(
                ctx, // HTTP Context
                null, // No GraphQL Environment
                Parameters.REQUEST_SOURCE.REST // Client Requested using REST API
        );

        // Run the EndpointInstance to process the request and return the result as JSON

        Object instanceResponse = null;
        try {
            log.debug("Handler class {} called", restEndpointInstance.getClass().getName());
            instanceResponse = restEndpointInstance.handleRequest(params);
        } catch (Exception e) {
            log.error("Exception was thrown during handling in RESTEndpointInstance", e);
            APIErrorExceptionResponse response = new APIErrorExceptionResponse();

            if (configuration.isDebugEnabled()) {
                response.setMessage(e.getMessage());
                response.setSimpleClassName(e.getClass().getSimpleName());

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                response.setStackTrace(sw.toString());
            } else {
                //Production mode
                response.setMessage("Internal Server Error. Please try again later.");
                response.setSimpleClassName(null);
                response.setStackTrace(null);
            }


            ctx.header("Content-Type", "application/json");
            ctx.status(500);
            instanceResponse = response;
        }

        if (instanceResponse != null) {
            log.debug("Serializing response of type {}", instanceResponse.getClass().getName());
            ctx.result(gson.toJson(instanceResponse));
        } else {
            //If response of our handler is null, return an HTTP 204 (No Content)
            log.debug("Response of handler {} is null, so set 204 (No Content) HTTP header", restEndpointInstance.getClass().getName());
            ctx.status(204);
        }
    }
}
