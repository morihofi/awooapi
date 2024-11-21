package net.fuxle.awooapi.server.intf;

import net.fuxle.awooapi.server.intf.handler.staticfiles.StaticFileServingHandler;

public abstract class WebServer {
    private final Router router = new Router();
    private final StaticFileServingHandler staticFileServingHandler = new StaticFileServingHandler(this);
    private Handler beforeRequestHandler = null;
    private Handler afterRequestHandler = null;
    private StaticFileServing staticFileServing = null;

    public static String getPoweredByValue() {
        return "AwooAPI/1.0";
    }

    public abstract void start(int port) throws Exception;

    public abstract void stop() throws Exception;

    public Router getRouter() {
        return router;
    }

    public Handler getBeforeRequestHandler() {
        return beforeRequestHandler;
    }

    public void setBeforeRequestHandler(Handler beforeRequestHandler) {
        this.beforeRequestHandler = beforeRequestHandler;
    }

    public Handler getAfterRequestHandler() {
        return afterRequestHandler;
    }

    public void setAfterRequestHandler(Handler afterRequestHandler) {
        this.afterRequestHandler = afterRequestHandler;
    }

    public StaticFileServing getStaticFileServing() {
        return staticFileServing;
    }

    public void setStaticFileServing(StaticFileServing staticFileServing) {
        this.staticFileServing = staticFileServing;
    }

    public StaticFileServingHandler getStaticFileServingHandler() {
        return staticFileServingHandler;
    }
}

