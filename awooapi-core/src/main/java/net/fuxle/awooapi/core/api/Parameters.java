package net.fuxle.awooapi.core.api;

import com.google.gson.Gson;
import graphql.schema.DataFetchingEnvironment;
import net.fuxle.awooapi.server.intf.HandlerContext;
import net.fuxle.awooapi.utilities.internals.IPAddressChecker;

import java.io.IOException;

/**
 * Utility class for handling request parameters and extracting relevant information from different request sources.
 */
public class Parameters {

    /**
     * The handler context for the current request.
     */
    private final HandlerContext handlerContext;

    /**
     * The GraphQL data fetching environment, available when the request source is GRAPH_QL.
     */
    private final DataFetchingEnvironment graphQLDatafetchingEnvironment;

    /**
     * Gson instance used for parsing JSON payloads.
     */
    private static final Gson gson = new Gson();

    /**
     * Enumeration representing the source of the request (e.g., REST or GraphQL).
     */
    public enum REQUEST_SOURCE {
        REST, GRAPH_QL
    }

    /**
     * Enumeration representing the type of REST argument (e.g., PATH or QUERY).
     */
    public enum REST_ARGUMENT_TYPE {
        PATH, QUERY
    }

    /**
     * Enumeration representing the origin of the IP address (e.g., internal or public).
     */
    public enum IP_SOURCE {
        INTERNAL_ADDRESS_SPACE, PUBLIC_ADDRESS_SPACE
    }

    /**
     * The source of the request (REST or GraphQL).
     */
    public final REQUEST_SOURCE source;

    /**
     * The origin of the request IP address (internal or public).
     */
    public final IP_SOURCE requestIpSource;

    /**
     * Constructs a Parameters object.
     *
     * @param handlerContext                 The handler context for the current request.
     * @param graphQLDatafetchingEnvironment The GraphQL data fetching environment, if applicable.
     * @param source                         The source of the request (REST or GraphQL).
     */
    public Parameters(HandlerContext handlerContext, DataFetchingEnvironment graphQLDatafetchingEnvironment, REQUEST_SOURCE source) {
        this.handlerContext = handlerContext;
        this.graphQLDatafetchingEnvironment = graphQLDatafetchingEnvironment;
        this.source = source;
        this.requestIpSource = IPAddressChecker.isPrivateIP(getRemoteIP()) ?
                IP_SOURCE.INTERNAL_ADDRESS_SPACE :
                IP_SOURCE.PUBLIC_ADDRESS_SPACE;
    }

    /**
     * Retrieves the request body and converts it to the specified class type.
     *
     * @param <T>         The type of the target class.
     * @param targetClass The class of the object to deserialize the request body into.
     * @return An object of the specified class type.
     * @throws IOException If there is an error reading the request body.
     */
    public <T> T getRequestBodyAs(Class<T> targetClass) throws IOException {
        return gson.fromJson(handlerContext.body(), targetClass);
    }

    /**
     * Retrieves the User-Agent header from the request.
     *
     * @return The User-Agent string, or null if it is not present in the request.
     */
    public String getUserAgent() {
        return handlerContext.request().getHeader("User-Agent");
    }

    /**
     * Retrieves the remote IP address of the client making the request.
     *
     * @return The IP address as a string.
     */
    public String getRemoteIP() {
        return handlerContext.request().getIP();
    }

    /**
     * Retrieves the argument value associated with the given parameter name and converts it to the specified target class type.
     *
     * @param <T>         The type of the return value.
     * @param paramName   The name of the parameter whose value needs to be fetched.
     * @param targetClass The class type to which the value should be converted.
     * @param type        The type of REST argument (PATH or QUERY).
     * @return The argument value converted to the specified target class type, or {@code null} if the value is not found.
     * @throws ClassCastException If the value cannot be cast to the specified target class type and cannot be converted.
     */
    public <T> T getArgument(String paramName, Class<T> targetClass, REST_ARGUMENT_TYPE type) {
        String value = null;
        switch (getSource()) {
            case REST -> {
                if (type == REST_ARGUMENT_TYPE.QUERY) {
                    value = handlerContext.queryParam(paramName);
                } else if (type == REST_ARGUMENT_TYPE.PATH) {
                    value = handlerContext.pathParam(paramName);
                }
            }
            case GRAPH_QL -> value = graphQLDatafetchingEnvironment.getArgument(paramName);
        }

        if (value == null) {
            return null;
        }

        if (targetClass.isAssignableFrom(value.getClass())) {
            return targetClass.cast(value);
        } else {
            return convertValue(value, targetClass);
        }
    }

    /**
     * Converts a value to the specified target class type.
     *
     * @param <T>         The type of the target class.
     * @param value       The value to convert.
     * @param targetClass The class of the target type.
     * @return The value converted to the specified target type.
     * @throws IllegalArgumentException If the conversion type is unsupported.
     */
    private <T> T convertValue(Object value, Class<T> targetClass) {
        if (targetClass == Integer.class) {
            return targetClass.cast(Integer.parseInt((String) value));
        } else if (targetClass == Double.class) {
            return targetClass.cast(Double.parseDouble((String) value));
        } else if (targetClass == Boolean.class) {
            return targetClass.cast(Boolean.parseBoolean((String) value));
        } else if (targetClass == String.class) {
            return targetClass.cast(value.toString());
        } else if (targetClass == Float.class) {
            return targetClass.cast(Float.parseFloat(value.toString()));
        } else if (targetClass == Long.class) {
            return targetClass.cast(Long.parseLong(value.toString()));
        }
        throw new IllegalArgumentException("Unsupported conversion type: " + targetClass.getName());
    }

    /**
     * Retrieves the value of the specified argument. If the argument is null, returns the provided default value.
     *
     * @param <T>          The type of the argument to be retrieved.
     * @param paramName    The name of the parameter to retrieve.
     * @param defaultValue The default value to return if the argument is null.
     * @param targetClass  The class of the target type.
     * @param type         The type of REST argument (PATH or QUERY).
     * @return The value of the specified argument or the default value if the argument is null.
     */
    public <T> T getArgumentOrDefault(String paramName, T defaultValue, Class<T> targetClass, REST_ARGUMENT_TYPE type) {
        T returnData = getArgument(paramName, targetClass, type);

        if (returnData == null) {
            return defaultValue;
        } else {
            return returnData;
        }
    }

    /**
     * Returns the Handler Context.
     *
     * @return The Handler Context associated with this request.
     */
    public HandlerContext getHandlerContext() {
        return handlerContext;
    }

    /**
     * Returns the GraphQL data-fetching environment.
     *
     * @return The GraphQL DataFetchingEnvironment, or null if the source is not GRAPH_QL.
     */
    public DataFetchingEnvironment getGraphQLDatafetchingEnvironment() {
        return graphQLDatafetchingEnvironment;
    }

    /**
     * Gets the source of the request (REST or GraphQL).
     *
     * @return The source of the request.
     */
    public REQUEST_SOURCE getSource() {
        return source;
    }

    /**
     * Determines whether the request comes from an internal or external IP address.
     *
     * @return The origin of the request IP (internal or public).
     */
    public IP_SOURCE getRequestIpSource() {
        return requestIpSource;
    }
}
