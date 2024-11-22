package net.fuxle.awooapi.server.intf;

/**
 * Represents a handler that processes HTTP requests within the server.
 * This interface is used to define the logic that should be executed when a specific endpoint is triggered.
 */
public interface Handler {

    /**
     * Handles an HTTP request based on the provided {@code HandlerContext}.
     * This method is responsible for processing the request and generating the appropriate response.
     *
     * @param context The {@code HandlerContext} containing the request, response, and routing information.
     * @throws Exception If an error occurs while handling the request.
     */
    void handle(HandlerContext context) throws Exception;
}
