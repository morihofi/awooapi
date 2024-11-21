package net.fuxle.awooapi.server.jetty;

import net.fuxle.awooapi.server.intf.WebServer;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class JettyWebServer extends WebServer {
    private Server server = null;
    private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void start(int port) throws Exception {
        if(isRunning()){
            throw new IllegalStateException("Cannot start server, because it is already running");
        }

        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Add AwooAPI Servlet
        context.addServlet(new ServletHolder(new AwooApiServlet(this)), "/");

        server.start();
        //server.join();
        log.info("\u2705 Server is ready");
    }

    @Override
    public void stop() throws Exception {
        if (isRunning()) {
            server.stop();
        }
    }

    private boolean isRunning() {
        return server != null && (!server.isStopped() || server.isStopping());
    }

}
