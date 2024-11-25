package net.fuxle.awooapi.common.plugin;

import net.fuxle.awooapi.common.plugin.impl.PluginEnvironment;
import net.fuxle.awooapi.common.plugin.intf.AbstractPlugin;
import net.fuxle.awooapi.server.intf.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Manages the lifecycle of plugins, including their registration, initialization, and unloading.
 */
public class AwooPluginManager {

    private static final Logger log = LoggerFactory.getLogger(AwooPluginManager.class);
    private final WebServer webServer;


    /**
     * Map to store registered plugin classes and their initialized instances.
     */
    private final Map<Class<? extends AbstractPlugin>, AbstractPlugin> plugins = new HashMap<>();


    public AwooPluginManager(WebServer webServer){
        this.webServer = webServer;

        Runtime.getRuntime().addShutdownHook(new Thread((this::unloadAllPlugins)));
    }

    public void unloadAllPlugins() {
        // Iterate over the entries of the map and unload plugins safely
        for (AbstractPlugin pluginInstance : new ArrayList<>(plugins.values())) {
            try {
                pluginInstance.unload();
            } catch (Exception e) {
                // Log the error in case the plugin fails to unload properly
                log.error("Failed to unload plugin: {}", pluginInstance.getClass().getName(), e);
            }
        }
        // Clear the plugins map after unloading all plugins
        plugins.clear();
    }


    /**
     * Registers a plugin class. Ensures that only one instance of a plugin class can be registered.
     * Dynamically creates an instance using the provided {@link PluginEnvironment} and initialization parameters.
     *
     * @param pluginClass The plugin class to register.
     * @param initParams  A map of initialization parameters (name-value pairs) to pass to the plugin.
     * @throws IllegalArgumentException if a plugin of the same class is already registered or instantiation fails.
     */
    public void registerPlugin(Class<? extends AbstractPlugin> pluginClass, Map<String, Object> initParams) {
        if (isPluginRegistered(pluginClass)) {
            throw new IllegalArgumentException("Plugin is already registered. Only one instance of a class can be registered!");
        }

        try {
            AbstractPlugin plugin = createPluginInstance(pluginClass, new PluginEnvironment(webServer, this, initParams));

            // Initialize the plugin
            plugin.initialize();

            plugins.put(pluginClass, plugin);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to instantiate plugin: " + pluginClass.getName(), e);
        }
    }

    /**
     * Unloads and removes a plugin based on its class.
     * Calls the {@code unload()} method of the plugin before removing it from the registry.
     *
     * @param pluginClass The class of the plugin to be unloaded.
     */
    public void unloadPlugin(Class<? extends AbstractPlugin> pluginClass) {
        Optional.ofNullable(plugins.remove(pluginClass)).ifPresent(AbstractPlugin::unload);
    }

    /**
     * Checks if a plugin of the specified class is already registered.
     *
     * @param pluginClass The plugin class to check.
     * @return {@code true} if the plugin class is already registered, {@code false} otherwise.
     */
    private boolean isPluginRegistered(Class<? extends AbstractPlugin> pluginClass) {
        return plugins.containsKey(pluginClass);
    }

    /**
     * Retrieves a plugin instance by its class type.
     *
     * @param <T>         The type of the plugin class extending {@link AbstractPlugin}.
     * @param pluginClass The {@link Class} object of the plugin to retrieve.
     * @return An {@link Optional} containing the plugin instance if found, or {@code Optional.empty()} if not.
     */
    public <T extends AbstractPlugin> Optional<T> getPluginInstanceByClass(Class<T> pluginClass) {
        return Optional.ofNullable(pluginClass.cast(plugins.get(pluginClass)));
    }

    /**
     * Creates a plugin instance dynamically using the provided environment and initialization parameters.
     *
     * @param pluginClass The plugin class to instantiate.
     * @param environment The shared {@link PluginEnvironment} instance to pass to the plugin's constructor.
     * @return A new instance of the plugin.
     * @throws Exception if instantiation fails.
     */
    private AbstractPlugin createPluginInstance(Class<? extends AbstractPlugin> pluginClass, PluginEnvironment environment) throws Exception {
        Constructor<?> constructor = pluginClass.getConstructor(PluginEnvironment.class);
        return (AbstractPlugin) constructor.newInstance(environment);
    }
}
