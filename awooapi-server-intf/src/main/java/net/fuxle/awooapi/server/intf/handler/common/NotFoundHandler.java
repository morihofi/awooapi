package net.fuxle.awooapi.server.intf.handler.common;

import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.HandlerContext;
import net.fuxle.awooapi.server.intf.HttpStatusCode;
import net.fuxle.awooapi.server.intf.WebServer;

/**
 * Handler for handling not found errors (404 status code).
 * This handler generates a generic HTML response indicating that the requested resource was not found.
 */
public class NotFoundHandler implements Handler {

    /**
     * Handles the request by setting the response status to 404 (Not Found)
     * and providing a simple HTML response body to indicate that the resource was not found.
     *
     * @param context The {@code HandlerContext} that provides information about the current request and response.
     */
    @Override
    public void handle(HandlerContext context) {
        context.status(HttpStatusCode.NOT_FOUND);
        context.contentType("text/html");
        context.result(String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>404 Not Found</title>
                </head>
                <body>
                    <h1>404 Not Found</h1>
                    <p>The requested resource could not be found on this server.</p>
                    <hr>
                    <address>%s</address>
                </body>
                </html>
                """, WebServer.getPoweredByValue()));
    }
}