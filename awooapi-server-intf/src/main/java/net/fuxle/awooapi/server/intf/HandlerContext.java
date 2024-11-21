package net.fuxle.awooapi.server.intf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public record HandlerContext(Request request, Response response) {
    public void header(String name, String value) {
        response.setHeader(name, value);
    }

    public String header(String name) {
        return request.getHeader(name);
    }

    public void contentType(String contentType) {
        header("Content-Type", contentType);
    }

    public String contentType() {
        return header("Content-Type");
    }

    public String queryParam(String paramName) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String pathParam(String paramName) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String body() throws IOException {
        return request.getBody();
    }

    public void status(int code) {
        response.setStatus(code);
    }

    public void status(HttpStatusCode code) {
        response.setStatus(code.getCode());
    }

    public void result(String data) {
        result(data.getBytes(StandardCharsets.UTF_8));
    }

    public void result(byte[] data) {
        response.setBodyBytes(data);
    }

    public void result() {
        result(""); // Empty body
    }

    public String path() {
        return request.getPath();
    }

    public Map<String, String> getResponseHeaders() {
        return response.getHeaders();
    }


}
