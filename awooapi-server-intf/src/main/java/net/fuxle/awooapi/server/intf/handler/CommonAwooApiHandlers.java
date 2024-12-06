package net.fuxle.awooapi.server.intf.handler;

import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.handler.common.AnyCORSBeforeHandler;
import net.fuxle.awooapi.server.intf.handler.common.InternalServerErrorHandler;
import net.fuxle.awooapi.server.intf.handler.common.NotFoundHandler;
import net.fuxle.awooapi.server.intf.handler.common.OptionsHandler;

/**
 * Contains common handlers used across the AwooAPI web server.
 * These handlers include support for CORS, handling HTTP OPTIONS requests, handling 404 (Not Found) errors,
 * and handling 500 (Internal Server Error) responses.
 */
public class CommonAwooApiHandlers {

    /**
     * Private constructor to prevent instantiation.
     */
    private CommonAwooApiHandlers() {
    }

    /**
     * Handler for HTTP OPTIONS requests.
     * This handler is used to provide CORS-related headers in response to OPTIONS preflight requests.
     */
    public static final Handler OPTIONS_HANDLER = new OptionsHandler();

    /**
     * Handler for 404 Not Found responses.
     * This handler is used to respond to requests for resources that cannot be found.
     */
    public static final Handler NOT_FOUND_HANDLER = new NotFoundHandler();

    /**
     * Handler to allow any CORS requests before handling an endpoint.
     * This handler is used to add the necessary CORS headers to allow cross-origin requests.
     */
    public static final Handler BEFORE_ALLOW_ANY_CORS_HANDLER = new AnyCORSBeforeHandler();

    /**
     * Handler for 500 Internal Server Error responses.
     * This handler is used to respond when an internal error occurs during request processing.
     */
    public static final ExceptionHandler INTERNAL_SERVER_ERROR_HANDLER = new InternalServerErrorHandler();
}
