package net.fuxle.awooapi.server.jetty;

import net.fuxle.awooapi.server.intf.WebServer;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;

public class JettyWebServer extends WebServer {
    private static final Logger log = LoggerFactory.getLogger(JettyWebServer.class);
    private Server server = new Server();
    private ServerConnector sslConnector;

    public Server getServer() {
        return server;
    }

    @Override
    public void start() throws Exception {

        // Set up SSL if configured
        if (getWebServerConfig().getSslConfig() != null && getWebServerConfig().getSslConfig().getPort() > 0) {
            log.info("HTTPS support is ENABLED");
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setSniRequired(getWebServerConfig().getSslConfig().isSniEnabled());
            sslContextFactory.setSslContext(getWebServerConfig().getSslConfig().getSslContext());

            // TODO: Implement client auth
            //sslContextFactory.setNeedClientAuth(getWebServerConfig().getSslConfig().isRequireClientAuth());

            HttpConfiguration httpsConfig = new HttpConfiguration();
            SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();
            secureRequestCustomizer.setSniHostCheck(getWebServerConfig().getSslConfig().isSniEnabled());
            httpsConfig.addCustomizer(secureRequestCustomizer);


            if (getWebServerConfig().getSslConfig().getMozillaConfig() != null) {
                log.info("Applying Mozilla SSL configuration");
                JettySslHelper.configureMozillaSsl(sslContextFactory, secureRequestCustomizer, getWebServerConfig().getSslConfig().getMozillaConfig());
            }

            sslConnector = new ServerConnector(
                    server,
                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory()
            );
            sslConnector.setPort(getWebServerConfig().getSslConfig().getPort());

            server.addConnector(sslConnector);
        }else{
            log.info("HTTPS support is DISABLED");
        }

        if (getWebServerConfig().getHttpPort() > 0) {
            log.info("HTTP support is ENABLED");
            // HTTP Configuration
            ServerConnector httpConnector = new ServerConnector(server);
            httpConnector.setPort(getWebServerConfig().getHttpPort());

            server.addConnector(httpConnector);
        } else {
            log.info("HTTP support is DISABLED");
        }


        // Configure our servlet
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Add AwooAPI Servlet
        context.addServlet(new ServletHolder(new AwooApiServlet(this)), "/");


        // Start Jetty
        server.start();

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

    /**
     * Hot reloads the SSL context with a new certificate.
     *
     * @param newSslContext The new {@code SSLContext} to apply.
     * @throws Exception If an error occurs during the update.
     */
    public void reloadSslContext(SSLContext newSslContext) throws Exception {
        if (getWebServerConfig().getSslConfig() == null) {
            throw new IllegalStateException("SSL is not configured for this server.");
        }

        // Update the SslConfig
        getWebServerConfig().getSslConfig().updateSslContext(newSslContext);

        // Dynamically update the SSL connector
        if (sslConnector != null) {
            SslContextFactory.Server sslContextFactory = sslConnector.getConnectionFactory(SslConnectionFactory.class).getSslContextFactory();
            sslContextFactory.setSslContext(getWebServerConfig().getSslConfig().getSslContext());

            // Restart the connector to apply changes
            sslConnector.stop();
            sslConnector.start();
        }
    }
}
