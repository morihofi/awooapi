package net.fuxle.awooapi.server.intf.handler.common;

import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.HandlerContext;
import net.fuxle.awooapi.server.intf.HttpStatusCode;
import net.fuxle.awooapi.server.intf.WebServer;
import net.fuxle.awooapi.server.intf.handler.ExceptionHandler;

/**
 * Handler for handling internal server errors (500 status code).
 * This handler generates a generic HTML response indicating that an internal server error occurred.
 */
public class InternalServerErrorHandler extends ExceptionHandler {

    /**
     * Handles the request by setting the response status to 500 (Internal Server Error)
     * and providing a simple HTML response body to indicate the error.
     *
     * @param context The {@code HandlerContext} that provides information about the current request and response.
     */
    @Override
    public void handle(Exception e, HandlerContext context) {
        context.status(HttpStatusCode.INTERNAL_SERVER_ERROR);
        context.contentType("text/html");
        context.result(String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>500 Internal Server Error</title>
                </head>
                <body>
                    <h1>500 Internal Server Error</h1>
                    <p>The server encountered an internal error and cannot process your request.</p>
                    <hr>
                    <address>%s</address>
                </body>
                </html>
                """, WebServer.getPoweredByValue()));
    }
}