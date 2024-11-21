package net.fuxle.awooapi.server.intf;

import java.io.IOException;

public interface Request {
    String getPath();
    String getMethod();
    String getHeader(String name);
    String getBody() throws IOException;
    String getIP();
    String getQueryParam(String name);
}
