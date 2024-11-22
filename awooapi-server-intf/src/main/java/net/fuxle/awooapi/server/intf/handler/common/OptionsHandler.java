package net.fuxle.awooapi.server.intf.handler.common;

import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.HandlerContext;
import net.fuxle.awooapi.server.intf.HttpStatusCode;

public class OptionsHandler implements Handler {
    @Override
    public void handle(HandlerContext context) {
        context.status(HttpStatusCode.NO_CONTENT);
        context.result();
    }
}
