package net.fuxle.awooapi.server.intf.handler.common;

import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.HandlerContext;
import net.fuxle.awooapi.server.intf.HttpStatusCode;

/**
 * Handler for handling HTTP OPTIONS requests.
 * This handler sets the response status to 204 (No Content) and provides an empty response body.
 */
public class OptionsHandler implements Handler {

    /**
     * Handles the request by setting the response status to 204 (No Content)
     * and providing an empty response body to indicate the request has been processed successfully.
     *
     * @param context The {@code HandlerContext} that provides information about the current request and response.
     */
    @Override
    public void handle(HandlerContext context) {
        context.status(HttpStatusCode.NO_CONTENT);
        context.result();
    }
}