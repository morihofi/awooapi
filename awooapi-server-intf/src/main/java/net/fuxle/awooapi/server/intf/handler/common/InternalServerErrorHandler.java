package net.fuxle.awooapi.server.intf.handler.common;

import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.HandlerContext;
import net.fuxle.awooapi.server.intf.HttpStatusCode;
import net.fuxle.awooapi.server.intf.WebServer;

public class InternalServerErrorHandler implements Handler {
    @Override
    public void handle(HandlerContext context) throws Exception {
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
