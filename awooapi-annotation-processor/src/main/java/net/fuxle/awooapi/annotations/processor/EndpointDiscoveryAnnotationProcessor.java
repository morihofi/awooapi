package net.fuxle.awooapi.annotations.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes({
                "net.fuxle.awooapi.annotations.RESTEndpoint",
                "net.fuxle.awooapi.annotations.WebSocketEndpoint",
                "net.fuxle.awooapi.annotations.GraphQLQuery",
                "net.fuxle.awooapi.annotations.MultiEndpoint"
        })
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class EndpointDiscoveryAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element : annotatedElements) {
                try {
                    generateMetadataFile(element);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate metadata: " + e.getMessage());
                }
            }
        }
        return true;
    }

    private void generateMetadataFile(Element element) throws IOException {
        String className = ((TypeElement) element).getQualifiedName().toString();
        String annotation = element.getAnnotationMirrors().toString();

        // Generate metadata file (e.g., as a JSON or properties file)
        Writer writer = processingEnv.getFiler().createSourceFile("META-INF/endpoint-metadata.properties").openWriter();
        writer.write("class=" + className + "\n");
        writer.write("annotation=" + annotation + "\n");
        writer.close();
    }
}