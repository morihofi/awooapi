package net.fuxle.awooapi.server.intf.handler;

import net.fuxle.awooapi.server.intf.Handler;
import net.fuxle.awooapi.server.intf.handler.common.AnyCORSBeforeHandler;
import net.fuxle.awooapi.server.intf.handler.common.InternalServerErrorHandler;
import net.fuxle.awooapi.server.intf.handler.common.NotFoundHandler;
import net.fuxle.awooapi.server.intf.handler.common.OptionsHandler;

public class CommonAwooApiHandlers {
    private CommonAwooApiHandlers() {
    }

    public static final Handler OPTIONS_HANDLER = new OptionsHandler();
    public static final Handler NOT_FOUND_HANDLER = new NotFoundHandler();
    public static final Handler BEFORE_ALLOW_ANY_CORS_HANDLER = new AnyCORSBeforeHandler();
    public static final Handler INTERNAL_SERVER_ERROR_HANDLER = new InternalServerErrorHandler();


}
