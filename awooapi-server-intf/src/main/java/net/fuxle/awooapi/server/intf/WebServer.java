package net.fuxle.awooapi.server.intf;

import net.fuxle.awooapi.server.intf.handler.staticfiles.StaticFileServingHandler;

/**
 * Represents an abstract web server that handles HTTP requests and responses.
 * The {@code WebServer} class provides the necessary methods to manage routing, serve static files,
 * and handle requests before and after endpoint processing.
 */
public abstract class WebServer {
    private final Router router = new Router();
    private final StaticFileServingHandler staticFileServingHandler = new StaticFileServingHandler(this);
    private Handler beforeRequestHandler = null;
    private Handler afterRequestHandler = null;
    private StaticFileServing staticFileServing = null;

    /**
     * Retrieves the "Powered By" value of the server.
     *
     * @return A {@code String} representing the server's powered by value.
     */
    public static String getPoweredByValue() {
        return "AwooAPI/1.0";
    }

    /**
     * Starts the web server on the given port.
     *
     * @param port The port number on which the server will listen for requests.
     * @throws Exception If an error occurs while starting the server.
     */
    public abstract void start(int port) throws Exception;

    /**
     * Stops the web server.
     *
     * @throws Exception If an error occurs while stopping the server.
     */
    public abstract void stop() throws Exception;

    /**
     * Retrieves the router instance used by this server.
     *
     * @return The {@code Router} instance.
     */
    public Router getRouter() {
        return router;
    }

    /**
     * Retrieves the handler to be executed before each request.
     *
     * @return The {@code Handler} to be executed before each request, or {@code null} if none is set.
     */
    public Handler getBeforeRequestHandler() {
        return beforeRequestHandler;
    }

    /**
     * Sets the handler to be executed before each request.
     *
     * @param beforeRequestHandler The {@code Handler} to be executed before each request.
     */
    public void setBeforeRequestHandler(Handler beforeRequestHandler) {
        this.beforeRequestHandler = beforeRequestHandler;
    }

    /**
     * Retrieves the handler to be executed after each request.
     *
     * @return The {@code Handler} to be executed after each request, or {@code null} if none is set.
     */
    public Handler getAfterRequestHandler() {
        return afterRequestHandler;
    }

    /**
     * Sets the handler to be executed after each request.
     *
     * @param afterRequestHandler The {@code Handler} to be executed after each request.
     */
    public void setAfterRequestHandler(Handler afterRequestHandler) {
        this.afterRequestHandler = afterRequestHandler;
    }

    /**
     * Retrieves the configuration for serving static files.
     *
     * @return The {@code StaticFileServing} configuration, or {@code null} if none is set.
     */
    public StaticFileServing getStaticFileServing() {
        return staticFileServing;
    }

    /**
     * Sets the configuration for serving static files.
     *
     * @param staticFileServing The {@code StaticFileServing} configuration to be set.
     */
    public void setStaticFileServing(StaticFileServing staticFileServing) {
        this.staticFileServing = staticFileServing;
    }

    /**
     * Retrieves the {@code StaticFileServingHandler} used by this server to serve static files.
     *
     * @return The {@code StaticFileServingHandler} instance.
     */
    public StaticFileServingHandler getStaticFileServingHandler() {
        return staticFileServingHandler;
    }
}
