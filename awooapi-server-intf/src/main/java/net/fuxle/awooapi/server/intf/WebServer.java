package net.fuxle.awooapi.server.intf;

import net.fuxle.awooapi.server.common.Router;
import net.fuxle.awooapi.server.common.StaticFileServing;
import net.fuxle.awooapi.server.common.WebServerConfig;
import net.fuxle.awooapi.server.intf.handler.ExceptionHandler;
import net.fuxle.awooapi.server.intf.handler.common.InternalServerErrorHandler;
import net.fuxle.awooapi.server.intf.handler.staticfiles.StaticFileServingHandler;

import javax.net.ssl.SSLContext;

/**
 * Represents an abstract web server that handles HTTP requests and responses.
 * The {@code WebServer} class provides the necessary methods to manage routing, serve static files,
 * and handle requests before and after endpoint processing.
 */
public abstract class WebServer {
    private final Router router = new Router();
    private final StaticFileServingHandler staticFileServingHandler = new StaticFileServingHandler(this);
    private ExceptionHandler exceptionHandler = new InternalServerErrorHandler();
    private StaticFileServing staticFileServing = null;
    private final WebServerConfig webServerConfig = new WebServerConfig();

    /**
     * Retrieves the "Powered By" value of the server.
     *
     * @return A {@code String} representing the server's powered by value.
     */
    public static String getPoweredByValue() {
        return "AwooAPI/1.0";
    }

    /**
     * Starts the web server with the configuration
     *
     * @throws Exception If an error occurs while starting the server.
     */
    public abstract void start() throws Exception;

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


    /**
     * Hot reloads the SSL context with a new certificate.
     *
     * @param newSslContext The new {@code SSLContext} to apply.
     * @throws Exception If an error occurs during the update.
     */
    public abstract void reloadSslContext(SSLContext newSslContext) throws Exception;


    public WebServerConfig getWebServerConfig() {
        return webServerConfig;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
}
