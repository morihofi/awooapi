package net.fuxle.awooapi.server.intf;

import net.fuxle.awooapi.annotations.HandlerType;

public class Endpoint {
    private final HandlerType type;
    private final String path;
    private final Handler handler;

    public Endpoint(HandlerType type, String path, Handler handler) {
        this.type = type;
        this.path = path;
        this.handler = handler;
    }

    public HandlerType getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public Handler getHandler() {
        return handler;
    }
}
