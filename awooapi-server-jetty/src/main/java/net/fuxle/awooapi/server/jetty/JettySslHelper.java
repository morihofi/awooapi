/*
 * Copyright (c) 2024 Moritz Hofmann <info@morihofi.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.fuxle.awooapi.server.jetty;

import net.fuxle.awooapi.server.common.mozillasslconfig.MozillaSslConfigHelper;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class JettySslHelper {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Creates an SSLContext with the specified certificate chain and key pair.
     *
     * @param certificateChain The certificate chain.
     * @param keyPair          The key pair.
     * @return An initialized SSLContext.
     * @throws KeyStoreException         If there is an issue with the keystore.
     * @throws CertificateException      If there is an issue with the certificate.
     * @throws IOException               If there is an issue reading the keystore.
     * @throws NoSuchAlgorithmException  If a required cryptographic algorithm is not available.
     * @throws KeyManagementException    If there is an issue with key management.
     * @throws UnrecoverableKeyException If the private key cannot be recovered.
     */
    public static SSLContext createSSLContext(X509Certificate[] certificateChain, KeyPair keyPair)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException,
            KeyManagementException, UnrecoverableKeyException, NoSuchProviderException {

        // Create a new KeyStore
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        // Add the certificate and key to the KeyStore
        keyStore.setKeyEntry("server", keyPair.getPrivate(), "".toCharArray(), certificateChain);

        // Initialize the KeyManagerFactory with the KeyStore
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "".toCharArray());

        // Initialize the TrustManagerFactory with the KeyStore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        // Create and initialize the SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS", BouncyCastleJsseProvider.PROVIDER_NAME);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }

    /**
     * Creates and configures an SSLContext for secure communication using the provided KeyStore, certificate alias, and key password.
     *
     * @param keyStore    The KeyStore containing the SSL certificate and private key.
     * @param alias       The alias of the certificate in the KeyStore.
     * @param keyPassword The password for the private key.
     * @return An SSLContext configured for secure communication.
     * @throws Exception If an error occurs while creating or configuring the SSLContext.
     */
    public static SSLContext createSSLContext(KeyStore keyStore, String alias, String keyPassword)
            throws Exception {

        // Create an instance of SslContextFactory
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

        // Set the KeyStore and passwords
        sslContextFactory.setKeyStore(keyStore);
        sslContextFactory.setKeyStorePassword(keyPassword);
        sslContextFactory.setKeyManagerPassword(keyPassword);

        // Set the alias for the certificate
        sslContextFactory.setCertAlias(alias);

        sslContextFactory.setProvider(BouncyCastleJsseProvider.PROVIDER_NAME);
        sslContextFactory.setProtocol("TLS");

        // Set the algorithm for the KeyManager
        sslContextFactory.setKeyManagerFactoryAlgorithm("PKIX");

        // Initialize SslContextFactory
        sslContextFactory.start();

        // Get the SSLContext object from SslContextFactory
        return sslContextFactory.getSslContext();
    }

    /**
     * Configures a Jetty Server with support for dynamic certificate updates for SSL and/or HTTP connectors.
     *
     * @param httpsPort   The port for HTTPS. Set to 0 to disable HTTPS.
     * @param httpPort    The port for HTTP. Set to 0 to disable HTTP.
     * @param sslContext  The SSL context to be used for HTTPS. Pass null to disable HTTPS.
     * @param server      The Jetty Server instance.
     * @param enableSniCheck Whether SNI Host Check should be enabled.
     * @param mozillaConfig Optional Mozilla SSL configuration for secure settings.
     * @return A configured Jetty Server instance.
     * @throws Exception If an error occurs during configuration.
     */
    public static Server configureJettyServer(int httpsPort, int httpPort, SSLContext sslContext, Server server,
                                              boolean enableSniCheck, MozillaSslConfigHelper.BasicConfiguration mozillaConfig) throws Exception {
        LOG.info("Reconfiguring Jetty Server...");

        // Gracefully stop existing connectors if necessary
        for (Connector connector : server.getConnectors()) {
            if (connector instanceof ServerConnector serverConnector && serverConnector.isStarted()) {
                LOG.info("Stopping connector on port: {}", serverConnector.getPort());
                serverConnector.stop();
            }
        }

        List<Connector> connectors = new ArrayList<>();

        if (httpsPort > 0 && sslContext != null) {
            LOG.info("Configuring HTTPS connector on port {}", httpsPort);

            HttpConfiguration httpsConfig = new HttpConfiguration();
            SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();
            secureRequestCustomizer.setSniHostCheck(enableSniCheck);
            httpsConfig.addCustomizer(secureRequestCustomizer);

            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setSslContext(sslContext);

            if (mozillaConfig != null) {
                LOG.info("Applying Mozilla SSL configuration");
                configureMozillaSsl(sslContextFactory, secureRequestCustomizer, mozillaConfig);
            }

            ServerConnector httpsConnector = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory(httpsConfig));
            httpsConnector.setPort(httpsPort);
            connectors.add(httpsConnector);
        } else {
            LOG.warn("HTTPS is disabled. This is not recommended for production.");
        }

        if (httpPort > 0) {
            LOG.info("Configuring HTTP connector on port {}", httpPort);
            ServerConnector httpConnector = new ServerConnector(server);
            httpConnector.setPort(httpPort);
            connectors.add(httpConnector);
        } else {
            LOG.info("HTTP is disabled");
        }

        // Update the server connectors
        server.setConnectors(connectors.toArray(new Connector[0]));

        // Start updated connectors
        for (Connector connector : server.getConnectors()) {
            if (connector instanceof ServerConnector) {
                LOG.info("Starting connector on port: {}", ((ServerConnector) connector).getPort());
                connector.start();
            }
        }

        LOG.info("Jetty Server reconfigured successfully");
        return server;
    }

    /**
     * Configures the SSL context factory with Mozilla's secure settings.
     *
     * @param sslContextFactory The SSL context factory to configure.
     * @param secureRequestCustomizer The secure request customizer for HSTS settings.
     * @param mozillaConfig The Mozilla SSL configuration.
     */
    public static void configureMozillaSsl(SslContextFactory.Server sslContextFactory,
                                            SecureRequestCustomizer secureRequestCustomizer,
                                            MozillaSslConfigHelper.BasicConfiguration mozillaConfig) {
        sslContextFactory.setExcludeProtocols();
        sslContextFactory.setExcludeCipherSuites();
        sslContextFactory.setRenegotiationAllowed(false);
        sslContextFactory.setUseCipherSuitesOrder(true);
        sslContextFactory.setIncludeCipherSuites(mozillaConfig.ciphers().toArray(new String[0]));
        sslContextFactory.setIncludeProtocols(mozillaConfig.protocols().toArray(new String[0]));
        secureRequestCustomizer.setStsMaxAge(mozillaConfig.hstsMinAge());
        secureRequestCustomizer.setStsIncludeSubDomains(false);
    }

    /**
     * Updates the SSL context of the HTTPS connector dynamically.
     *
     * @param server     The Jetty Server instance.
     * @param sslContext The new SSL context to apply.
     * @throws Exception If an error occurs during the update.
     */
    public static void updateSslContext(Server server, SSLContext sslContext) throws Exception {
        LOG.info("Updating SSL context dynamically");

        for (Connector connector : server.getConnectors()) {
            if (connector instanceof ServerConnector serverConnector) {
                ConnectionFactory connectionFactory = serverConnector.getDefaultConnectionFactory();
                if (connectionFactory instanceof SslConnectionFactory sslConnectionFactory) {
                    SslContextFactory.Server sslContextFactory = (SslContextFactory.Server) sslConnectionFactory.getSslContextFactory();
                    sslContextFactory.setSslContext(sslContext);

                    // Restart the connector to apply the new SSL context
                    if (serverConnector.isStarted()) {
                        LOG.info("Restarting HTTPS connector on port {}", serverConnector.getPort());
                        serverConnector.stop();
                        serverConnector.start();
                    }
                    LOG.info("SSL context updated successfully");
                    return;
                }
            }
        }
        LOG.warn("No HTTPS connector found to update SSL context");
    }


    private JettySslHelper() {
    }
}
