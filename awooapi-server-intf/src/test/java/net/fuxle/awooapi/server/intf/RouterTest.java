package net.fuxle.awooapi.server.intf;

import net.fuxle.awooapi.annotations.HandlerType;
import net.fuxle.awooapi.server.common.Router;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Router class.
 */
public class RouterTest {
    private Router router;
    private Endpoint testEndpoint;
    private Handler testHandler;

    @BeforeEach
    public void setUp() {
        router = new Router();
        testHandler = (handlerContext) -> {};
        testEndpoint = new Endpoint(HandlerType.GET,"/test", testHandler);
    }

    @Test
    public void testAddAndRetrieveStaticHandler() throws Exception {
        // Add a static endpoint
        router.addHandler(testEndpoint);

        // Retrieve handler by path and method
        Handler retrievedHandler = router.getHandler("/test", "GET", null);
        assertNotNull(retrievedHandler, "Handler should be retrieved for the static path.");
        assertEquals(testHandler, retrievedHandler, "The retrieved handler should match the added handler.");
    }

    @Test
    public void testAddAndRetrieveVariableHandler() throws Exception {
        // Add a variable endpoint
        Endpoint endpointWithVariable = new Endpoint(HandlerType.GET,"/api/{id}", testHandler);
        router.addHandler(endpointWithVariable);

        // Retrieve handler by variable path
        Handler retrievedHandler = router.getHandler("/api/123", "GET", null);
        assertNotNull(retrievedHandler, "Handler should be retrieved for the variable path.");
        assertEquals(testHandler, retrievedHandler, "The retrieved handler should match the added handler.");
    }

    @Test
    public void testHandlerNotFound() throws Exception {
        // Test handler not found for unregistered path
        Handler handler = router.getHandler("/notfound", "GET", null);
        assertNull(handler, "Handler should be null for an unregistered path.");
    }

    @Test
    public void testRemoveHandler() throws Exception {
        // Add and then remove an endpoint
        router.addHandler(testEndpoint);
        router.removeHandler("/test");

        // Ensure the handler is removed
        Handler removedHandler = router.getHandler("/test", "GET", null);
        assertNull(removedHandler, "Handler should be null after being removed.");
    }

    @Test
    public void testRemoveVariableHandler() throws Exception {
        // Add a variable endpoint and then remove it
        Endpoint variableEndpoint = new Endpoint(HandlerType.GET, "/api/{id}", testHandler);
        router.addHandler(variableEndpoint);
        router.removeHandler("/api/{id}");

        // Ensure the handler is removed
        Handler removedHandler = router.getHandler("/api/123", "GET", null);
        assertNull(removedHandler, "Handler should be null for the removed variable path.");
    }

    @Test
    public void testGetPathParam() {
        // Add a variable endpoint
        Endpoint variableEndpoint = new Endpoint(HandlerType.GET, "/api/{id}", testHandler);
        router.addHandler(variableEndpoint);

        // Retrieve path parameter
        String paramValue = router.getPathParam("/api/123", "id");
        assertEquals("123", paramValue, "The path parameter value should be correctly retrieved.");
    }

    @Test
    public void testGetPathParamNotFound() {
        // Add a variable endpoint
        Endpoint variableEndpoint = new Endpoint(HandlerType.GET, "/api/{id}", testHandler);
        router.addHandler(variableEndpoint);

        // Retrieve non-existent parameter
        String paramValue = router.getPathParam("/api/123", "name");
        assertNull(paramValue, "Path parameter should be null if it does not exist.");
    }

    @Test
    public void testGetHandlerWithDifferentHttpMethods() throws Exception {
        // Add endpoints with different methods
        Endpoint getEndpoint = new Endpoint(HandlerType.GET, "/resource", testHandler);
        Endpoint postEndpoint = new Endpoint(HandlerType.POST, "/resource", testHandler);
        router.addHandler(getEndpoint);
        router.addHandler(postEndpoint);

        // Retrieve handlers for different methods
        Handler getHandler = router.getHandler("/resource", "GET", null);
        Handler postHandler = router.getHandler("/resource", "POST",null);

        assertNotNull(getHandler, "GET handler should be found.");
        assertNotNull(postHandler, "POST handler should be found.");
        assertEquals(testHandler, getHandler, "GET handler should match the added handler.");
        assertEquals(testHandler, postHandler, "POST handler should match the added handler.");
    }
}
