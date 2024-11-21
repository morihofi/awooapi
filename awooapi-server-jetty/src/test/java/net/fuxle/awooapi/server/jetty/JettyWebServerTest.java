package net.fuxle.awooapi.server.jetty;

import net.fuxle.awooapi.annotations.HandlerType;
import net.fuxle.awooapi.server.intf.Endpoint;
import net.fuxle.awooapi.server.intf.WebServer;

import static org.junit.jupiter.api.Assertions.*;

class JettyWebServerTest {
    public static void main(String[] args) throws Exception {
        JettyWebServer webServer = new JettyWebServer();

        // Beispiel: Einen Handler hinzufügen
        webServer.getRouter().addHandler(new Endpoint(
                HandlerType.GET,
                "/hello",
                context -> {
                    context.contentType("text/plain");
                    context.result("Hello from custom handler!");
                }
        ));

        webServer.start(8080);
    }
}