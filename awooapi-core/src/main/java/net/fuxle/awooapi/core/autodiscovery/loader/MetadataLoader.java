package net.fuxle.awooapi.core.autodiscovery.loader;

import net.fuxle.awooapi.annotations.MultiEndpoint;
import net.fuxle.awooapi.core.autodiscovery.ClassDiscovery;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MetadataLoader {
    private final ClassDiscovery classDiscovery;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public MetadataLoader(ClassDiscovery classDiscovery) {
        this.classDiscovery = classDiscovery;
    }

    public List<String> requireMetadata() {
        List<String> metadata;
        try {
            metadata = loadMetadata();
        } catch (IOException e) {
            log.warn("Metadata file not found, falling back to runtime scanning.");
            metadata = performRuntimeScan();
        }
        return metadata;
    }

    public List<String> loadMetadata() throws IOException {
        List<String> metadata = new ArrayList<>();
        File metadataFile = new File("META-INF/endpoint-metadata.properties");
        if (!metadataFile.exists()) {
            throw new IOException("Metadata file not found");
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                metadata.add(line);
            }
        }
        return metadata;
    }

    public List<String> performRuntimeScan() {
        log.info("\uD83D\uDD0E Performing runtime scan for component classes ...");
        Reflections reflections = classDiscovery.createReflections();
        Set<Class<?>> restEndpointClasses = reflections.getTypesAnnotatedWith(MultiEndpoint.class);
        List<String> metadata = new ArrayList<>();
        for (Class<?> clazz : restEndpointClasses) {
            metadata.add("class=" + clazz.getName() + "\nannotation=RESTEndpoint");
        }
        return metadata;
    }
}
