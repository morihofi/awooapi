package net.fuxle.awooapi.server.jetty;

import net.fuxle.awooapi.annotations.HandlerType;
import net.fuxle.awooapi.server.intf.Endpoint;

class JettyWebServerTest {
    public static void main(String[] args) throws Exception {
        JettyWebServer webServer = new JettyWebServer();
        webServer.getWebServerConfig().setHttpPort(8080);

        // Beispiel: Einen Handler hinzufügen
        webServer.getRouter().addHandler(new Endpoint(
                HandlerType.GET,
                "/hello",
                context -> {
                    context.contentType("text/plain");
                    context.result("Hello from custom handler!");
                }
        ));

        webServer.start();
    }
}