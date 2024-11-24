package net.fuxle.awooapi.common.plugin.impl;

import net.fuxle.awooapi.common.plugin.AwooPluginManager;
import net.fuxle.awooapi.server.intf.WebServer;

import java.util.HashMap;
import java.util.Map;

public class PluginEnvironment {
    private final WebServer webServer;
    private final AwooPluginManager pluginManager;
    private final Map<String,Object> parameter;

    public PluginEnvironment(WebServer webServer, AwooPluginManager pluginManager, Map<String, Object> parameter) {
        this.webServer = webServer;
        this.pluginManager = pluginManager;
        this.parameter = parameter;
    }

    public WebServer getWebServer() {
        return webServer;
    }

    public AwooPluginManager getPluginManager() {
        return pluginManager;
    }

    public Map<String, Object> getParameter() {
        return parameter;
    }
}
