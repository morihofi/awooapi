package net.fuxle.awooapi.server.jetty;

import jakarta.servlet.http.HttpServletRequest;
import net.fuxle.awooapi.server.intf.Request;

import java.io.IOException;

public class HttpRequestWrapper implements Request {
    private final HttpServletRequest request;

    public HttpRequestWrapper(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getPath() {
        return request.getRequestURI();
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public String getBody() throws IOException {
        return new String(request.getInputStream().readAllBytes());
    }

    @Override
    public String getIP() {
        return request.getRemoteAddr();
    }


    @Override
    public String getQueryParam(String name) {
        return request.getParameter(name);
    }

    @Override
    public byte[] getBodyBytes() throws IOException {
        return request.getInputStream().readAllBytes();
    }
}
