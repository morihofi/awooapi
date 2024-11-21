package net.fuxle.awooapi.server.jetty;

import net.fuxle.awooapi.server.intf.WebServer;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;

import java.io.IOException;

public class JettyWebServer extends WebServer {
    private Server server = null;

    @Override
    public void start(int port) throws Exception {
        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Add AwooAPI Servlet
        context.addServlet(new ServletHolder(new AwooApiServlet(this)), "/");

        server.start();
        //server.join();
    }

    @Override
    public void stop() throws Exception {
        if (server != null && (!server.isStopped() || server.isStopping())) {
            server.stop();
        }
    }
}
