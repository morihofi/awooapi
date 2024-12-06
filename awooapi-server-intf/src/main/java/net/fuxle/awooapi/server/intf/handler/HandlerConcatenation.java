package net.fuxle.awooapi.server.intf.handler;

import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.HandlerContext;

import java.util.ArrayList;
import java.util.List;

public class HandlerConcatenation implements Handler {

    private List<Handler> handlers = new ArrayList<>();

    public HandlerConcatenation() {
    }

    public HandlerConcatenation(List<Handler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handle(HandlerContext context) throws Exception {
        for (Handler h : handlers){
            h.handle(context);
        }
    }
}
