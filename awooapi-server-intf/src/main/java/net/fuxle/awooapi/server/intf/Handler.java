package net.fuxle.awooapi.server.intf;

public interface Handler {
    void handle(HandlerContext context) throws Exception;
}
