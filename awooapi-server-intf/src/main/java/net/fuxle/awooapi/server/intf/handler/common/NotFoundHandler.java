package net.fuxle.awooapi.server.intf.handler.common;

import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.HandlerContext;
import net.fuxle.awooapi.server.intf.HttpStatusCode;
import net.fuxle.awooapi.server.intf.WebServer;

public class NotFoundHandler implements Handler {
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
