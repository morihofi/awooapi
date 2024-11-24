package net.fuxle.awooapi.core.autodiscovery.dispatcher;

import com.google.gson.Gson;
import net.fuxle.awooapi.RuntimeConfiguration;
import net.fuxle.awooapi.core.templates.AbstractEndpoint;
import net.fuxle.awooapi.core.api.Parameters;
import net.fuxle.awooapi.core.exceptions.AwooApiHandlerExecutionException;
import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.HandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class RESTDispatcher<T> implements Handler {

    /**
     * The instance of {@link AbstractEndpoint} used to process REST requests.
     */
    private final AbstractEndpoint<T> restEndpointInstance;
    private final RuntimeConfiguration configuration;

    private static final Logger log = LoggerFactory.getLogger(RESTDispatcher.class);
    private static final Gson gson = new Gson();
    private static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * Constructs a new RESTDispatcher with the provided instance of {@link AbstractEndpoint}.
     *
     * @param restEndpointInstance The instance of {@link AbstractEndpoint} to be used for processing REST requests.
     * @param configuration The configuration used for runtime settings.
     */
    public RESTDispatcher(AbstractEndpoint<T> restEndpointInstance, RuntimeConfiguration configuration) {
        this.restEndpointInstance = restEndpointInstance;
        this.configuration = configuration;
    }

    /**
     * Handles the incoming REST request, processes it using the restEndpointInstance, and returns the response in JSON format.
     *
     * @param ctx The HTTP context for handling the request.
     */
    @Override
    public void handle(HandlerContext ctx) throws AwooApiHandlerExecutionException {
        // Create Parameters for processing the REST request
        Parameters params = new Parameters(
                ctx, // HTTP Context
                null, // No GraphQL Environment
                Parameters.REQUEST_SOURCE.REST // Client Requested using REST API
        );

        T instanceResponse;
        try {
            log.debug("Handler class {} called with parameters: {}", restEndpointInstance.getClass().getName(), params);
            instanceResponse = restEndpointInstance.handleRequest(params);
        } catch (Exception e) {
            throw new AwooApiHandlerExecutionException("Error running handler", e);
        }

        if (instanceResponse != null) {
            log.debug("Serializing response of type {}", instanceResponse.getClass().getName());
            setJsonResponseHeader(ctx);
            ctx.result(gson.toJson(instanceResponse));
        } else {
            // If response of our handler is null, return an HTTP 204 (No Content)
            log.debug("Response of handler {} is null, setting 204 (No Content) HTTP status", restEndpointInstance.getClass().getName());
            ctx.status(204);
        }
    }

    /**
     * Handles exceptions by creating a response based on the current configuration.
     *
     * @param e The exception that was thrown.
     * @return An {@link APIErrorExceptionResponse} containing the error details.
     */
    private APIErrorExceptionResponse handleException(Exception e) {
        APIErrorExceptionResponse response = new APIErrorExceptionResponse();

        if (configuration.getDebugConfig().isDebugEnabled()) {
            response.setMessage(e.getMessage());
            response.setSimpleClassName(e.getClass().getSimpleName());

            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
            }
            response.setStackTrace(sw.toString());
        } else {
            // Production mode
            response.setMessage("Internal Server Error. Please try again later.");
            response.setSimpleClassName(null);
            response.setStackTrace(null);
        }
        return response;
    }

    /**
     * Sets the Content-Type header to application/json for the response.
     *
     * @param ctx The HTTP context for handling the request.
     */
    private void setJsonResponseHeader(HandlerContext ctx) {
        ctx.header("Content-Type", CONTENT_TYPE_JSON);
    }

    public AbstractEndpoint<T> getRestEndpointInstance() {
        return restEndpointInstance;
    }
}