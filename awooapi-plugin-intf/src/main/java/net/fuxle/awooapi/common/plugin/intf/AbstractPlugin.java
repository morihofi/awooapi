package net.fuxle.awooapi.common.plugin.intf;

import net.fuxle.awooapi.common.plugin.impl.PluginEnvironment;

public abstract class AbstractPlugin {
    private final PluginEnvironment pluginEnvironment;

    public AbstractPlugin(PluginEnvironment pluginEnvironment) {
        this.pluginEnvironment = pluginEnvironment;
    }

    public PluginEnvironment getPluginEnvironment() {
        return pluginEnvironment;
    }

    public abstract void initialize();
    public abstract void unload();
}
