package net.fuxle.awooapi.server.common;

import net.fuxle.awooapi.server.common.mozillasslconfig.MozillaSslConfigHelper;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents the SSL configuration for the server, with support for hot reloading.
 */
public class SslConfig {
    private volatile SSLContext sslContext;
    private final ReentrantLock lock = new ReentrantLock();
    private final int port;
    private final boolean isSniEnabled;
    private final MozillaSslConfigHelper.BasicConfiguration mozillaConfig;

    /**
     * Creates a new {@code SslConfig} instance.
     *
     * @param sslContext       The {@code SSLContext} to use for secure connections.
     */
    public SslConfig(SSLContext sslContext, int port, boolean isSniEnabled, MozillaSslConfigHelper.BasicConfiguration mozillaConfig) {
        this.sslContext = sslContext;
        this.port = port;
        this.isSniEnabled = isSniEnabled;
        this.mozillaConfig = mozillaConfig;
    }

    public SslConfig(SSLContext sslContext, int port, boolean isSniEnabled) throws IOException {
        this(sslContext,port,isSniEnabled, MozillaSslConfigHelper.getConfigurationGuidelinesForVersion("5.7", MozillaSslConfigHelper.CONFIGURATION.INTERMEDIATE));
    }

    /**
     * Retrieves the {@code SSLContext} for secure connections.
     *
     * @return The current {@code SSLContext}.
     */
    public SSLContext getSslContext() {
        return sslContext;
    }

    /**
     * Updates the {@code SSLContext} dynamically (hot reload).
     *
     * @param newSslContext The new {@code SSLContext} to use.
     */
    public void updateSslContext(SSLContext newSslContext) {
        lock.lock();
        try {
            this.sslContext = newSslContext;
        } finally {
            lock.unlock();
        }
    }


    public int getPort() {
        return port;
    }

    public boolean isSniEnabled() {
        return isSniEnabled;
    }

    public MozillaSslConfigHelper.BasicConfiguration getMozillaConfig() {
        return mozillaConfig;
    }
}
