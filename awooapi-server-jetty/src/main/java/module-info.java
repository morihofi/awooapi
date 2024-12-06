module awooapi.server.jetty {
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.ee10.servlet;
    requires awooapi.server.intf;
    requires awooapi.annotations;
    requires org.bouncycastle.tls;
    requires awooapi.cryptography;

    // Benötigte Abhängigkeiten für Jetty-Server
    requires org.eclipse.jetty.util;
    requires org.eclipse.jetty.http;
    requires org.eclipse.jetty.io;

    // Falls SSL erforderlich ist
    requires org.eclipse.jetty.alpn.server; // Für HTTP/2
    requires java.base; // Standardmäßig erforderlich
    requires java.net.http; // Für HTTP-Client


}
