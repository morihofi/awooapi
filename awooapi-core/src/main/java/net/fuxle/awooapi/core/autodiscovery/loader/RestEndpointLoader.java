package net.fuxle.awooapi.core.autodiscovery.loader;

import net.fuxle.awooapi.annotations.HandlerType;
import net.fuxle.awooapi.annotations.MultiEndpoint;
import net.fuxle.awooapi.core.autodiscovery.ClassDiscovery;
import net.fuxle.awooapi.core.templates.AbstractEndpoint;
import net.fuxle.awooapi.core.autodiscovery.dispatcher.RESTDispatcher;
import net.fuxle.awooapi.core.exceptions.AwooApiException;
import net.fuxle.awooapi.server.intf.Endpoint;
import net.fuxle.awooapi.server.intf.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RestEndpointLoader {
    private final ClassDiscovery classDiscovery;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public RestEndpointLoader(ClassDiscovery classDiscovery) {
        this.classDiscovery = classDiscovery;
    }

    public void loadAndRegisterRestEndpoints(List<String> metadata, WebServer server) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, AwooApiException {
        Set<Endpoint> endpoints = performRuntimeScanForClasses();

        for (Endpoint e : endpoints){
            log.info("\uD83D\uDD0C Registering REST {}-Endpoint at {} --> {}",e.getType().toString(), e.getPath(), ((RESTDispatcher<?>) e.getHandler()).getRestEndpointInstance().getClass().getName());
            server.getRouter().addHandler(e);
        }

    }



    protected Set<Endpoint> performRuntimeScanForClasses() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, AwooApiException {
        Set<Endpoint> restHandler = new HashSet<>();

        // Durchlaufe alle gefundenen Klassen
        for (Class<? extends AbstractEndpoint<?>> clazz : LoaderHelper.getEndpointClasses(classDiscovery.
                getConfig().getSearchConfig().getPackagePrefix(), AbstractEndpoint.class, classDiscovery.createReflections())) {

            // Erstelle eine neue Instanz der Klasse mit den gegebenen Parametern (Constructor wird auto. aufgerufen)
            AbstractEndpoint<?> multiEndpointInstance = createInstanceForClass(clazz);

            HandlerType[] types = clazz.getAnnotation(MultiEndpoint.class).restType();
            String path = clazz.getAnnotation(MultiEndpoint.class).restPath();
            String[] apiVersions = clazz.getAnnotation(MultiEndpoint.class).restVersionPrefix();
            boolean debugOnly = clazz.getAnnotation(MultiEndpoint.class).debugOnly();

            if (debugOnly && !classDiscovery.getConfig().getDebugConfig().isDebugEnabled()) {
                //Production mode, don't enable plugins that should only run in debug mode
                continue;
            }

            // Validate the Path
            LoaderHelper.validatePath(path);

            for (String apiVersion : apiVersions) {

                path = LoaderHelper.constructPath(classDiscovery.getConfig().getApiConfig().getApiPrefix(), apiVersion, path);

                for (HandlerType type : types) {
                    restHandler.add(new Endpoint(type, path, new RESTDispatcher<>(multiEndpointInstance, classDiscovery.getConfig())));
                }
            }

        }

        return restHandler;
    }


    private AbstractEndpoint<?> createInstanceForClass(Class<? extends AbstractEndpoint<?>> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return clazz.getDeclaredConstructor().newInstance();
    }


}
