package net.fuxle.awooapi.server.common;

public class WebServerConfig {
    private int httpPort = 80;
    private SslConfig sslConfig;

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpPort() {
        return httpPort;
    }


    /**
     * Retrieves the SSL configuration for the server.
     *
     * @return The {@code SslConfig} instance, or {@code null} if SSL is not configured.
     */
    public SslConfig getSslConfig() {
        return sslConfig;
    }

    public void setSslConfig(SslConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

}
