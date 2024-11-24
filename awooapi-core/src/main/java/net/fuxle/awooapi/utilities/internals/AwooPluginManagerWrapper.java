package net.fuxle.awooapi.utilities.internals;

import net.fuxle.awooapi.AwooApplication;
import net.fuxle.awooapi.common.plugin.AwooPluginManager;


public class AwooPluginManagerWrapper {
    private final AwooPluginManager pluginManager;

    public AwooPluginManagerWrapper(AwooApplication awooApplication) {
        this.pluginManager = new AwooPluginManager(awooApplication.getWebServer());
    }

    public AwooPluginManager getPluginManager() {
        return pluginManager;
    }

    

}
