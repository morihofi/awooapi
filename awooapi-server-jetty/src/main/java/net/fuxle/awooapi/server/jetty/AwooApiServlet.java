package net.fuxle.awooapi.server.jetty;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.fuxle.awooapi.annotations.HandlerType;
import net.fuxle.awooapi.server.intf.*;
import net.fuxle.awooapi.server.intf.handler.CommonAwooApiHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AwooApiServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AwooApiServlet.class);
    private final WebServer webServer;

    public AwooApiServlet(WebServer webServer) {
        this.webServer = webServer;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        String method = req.getMethod();
        String path = req.getRequestURI();
        HandlerContext context = getHandlerContext(req, resp);


        resp.setHeader("X-Powered-By", WebServer.getPoweredByValue());

        logger.info("Incoming request: method={}, path={}", method, path);

        // Let the Handler do its thing
        try {
            Handler handler = HandlerType.valueOf(method) ==
                    HandlerType.OPTIONS ?
                    CommonAwooApiHandlers.OPTIONS_HANDLER :
                    webServer.getRouter().getHandler(path, method, context);


            if (handler == null) {
                // No handler found, maybe a static file was requested
                if (webServer.getStaticFileServing() != null && webServer.getStaticFileServing().existsFileOrDirectory(path)) {
                    // We found something, let's serve that
                    webServer.getStaticFileServingHandler().handle(context);
                } else {
                    // No static file serving path found or configured, serve 404
                    CommonAwooApiHandlers.NOT_FOUND_HANDLER.handle(context);
                    logger.info("Handles 404 request for path: {}", path);
                }

            } else {
                handler.handle(context);
                logger.info("Successfully handled request for path: {}", path);
            }

        } catch (Exception e) {
            logger.error("Error while handling request for path: {}", path, e);
            try {
                webServer.getExceptionHandler().handle(e, context);
            } catch (Exception ignored) {
            }

        }
    }

    private HandlerContext getHandlerContext(HttpServletRequest req, HttpServletResponse resp) {
        return new HandlerContext(
                new HttpRequestWrapper(req),
                new HttpResponseWrapper(resp),
                webServer.getRouter()
        );
    }
}
