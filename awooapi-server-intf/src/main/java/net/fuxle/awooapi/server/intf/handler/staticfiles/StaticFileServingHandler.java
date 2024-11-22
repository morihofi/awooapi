package net.fuxle.awooapi.server.intf.handler.staticfiles;

import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.HandlerContext;
import net.fuxle.awooapi.server.intf.StaticFileServing;
import net.fuxle.awooapi.server.intf.WebServer;

public class StaticFileServingHandler implements Handler {

    private final WebServer webServer;

    public StaticFileServingHandler(WebServer webServer) {
        this.webServer = webServer;
    }


    @Override
    public void handle(HandlerContext context) {

        StaticFileServing staticFileServing = webServer.getStaticFileServing();
        if(staticFileServing == null){
            throw new IllegalStateException("Cannot serve static file, due to non configured static file configuration.");
        }

        if(staticFileServing.existsFileOrDirectory(context.path())){
            throw new IllegalStateException("Path does not exist");
        }

        if(staticFileServing.getPathType(context.path()) != StaticFileServing.PATH_TYPE.FILE){
            throw new UnsupportedOperationException("Only file serving is currently implemented");
        }

        context.result(staticFileServing.getFileContents(context.path()));

    }
}
