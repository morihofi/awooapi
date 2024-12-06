package net.fuxle.awooapi.server.intf.handler.staticfiles;

import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.HandlerContext;
import net.fuxle.awooapi.server.common.StaticFileServing;
import net.fuxle.awooapi.server.intf.WebServer;

/**
 * A handler for serving static files from the server.
 * The {@code StaticFileServingHandler} is responsible for handling requests for static resources,
 * such as HTML, CSS, JavaScript, images, and other static content.
 */
public class StaticFileServingHandler implements Handler {

    private final WebServer webServer;

    /**
     * Constructs a new {@code StaticFileServingHandler} for the given {@code WebServer}.
     *
     * @param webServer The {@code WebServer} instance to use for static file serving.
     */
    public StaticFileServingHandler(WebServer webServer) {
        this.webServer = webServer;
    }

    /**
     * Handles the request to serve a static file.
     * The handler checks if the static file configuration is present, verifies if the requested path exists,
     * and serves the content if it is a file.
     *
     * @param context The {@code HandlerContext} that provides information about the current request and response.
     * @throws IllegalStateException If static file serving is not configured or the path does not exist.
     * @throws UnsupportedOperationException If the requested path is not a file.
     */
    @Override
    public void handle(HandlerContext context) {

        StaticFileServing staticFileServing = webServer.getStaticFileServing();
        if(staticFileServing == null){
            throw new IllegalStateException("Cannot serve static file, due to non configured static file configuration.");
        }

        if(staticFileServing.existsFileOrDirectory(context.path())){
            throw new IllegalStateException("Path does not exist");
        }

        if(staticFileServing.getPathType(context.path()) != StaticFileServing.PATH_TYPE.FILE){
            throw new UnsupportedOperationException("Only file serving is currently implemented");
        }

        context.result(staticFileServing.getFileContents(context.path()));

    }
}
