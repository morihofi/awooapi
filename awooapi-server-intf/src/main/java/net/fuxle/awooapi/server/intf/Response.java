package net.fuxle.awooapi.server.intf;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class Response {
    private int status = 200; // OK
    private byte[] body;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public abstract void setHeader(String name, String value);

    public abstract String getHeader(String name);

    public void setBody(String body) {
        setBodyBytes(body.getBytes(StandardCharsets.UTF_8));
    }

    public abstract void setBodyBytes(byte[] data);
    public byte[] getBody() {
        return body;
    }

    public abstract Map<String, String> getHeaders();


}
