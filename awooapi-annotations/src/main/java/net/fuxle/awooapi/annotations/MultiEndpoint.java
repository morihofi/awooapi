package net.fuxle.awooapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MultiEndpoint {

    /**
     * Field name in GraphQL Schema
     */
    String graphQLFieldName();

    GraphQlFieldType graphQLFieldType();

    /**
     * HTTP Types when using REST Interface
     */
    HandlerType[] restType();

    /**
     * API Path when using REST Interface
     */
    String restPath();

    /**
     * API Version Prefix when using REST Interface (e.g. "v1" for example)
     */
    String[] restVersionPrefix() default "";

    boolean debugOnly() default false;

}
