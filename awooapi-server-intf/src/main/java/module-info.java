module awooapi.server.intf {
    requires awooapi.annotations;
    exports net.fuxle.awooapi.server.intf;
    exports net.fuxle.awooapi.server.intf.handler;
    exports net.fuxle.awooapi.server.intf.handler.staticfiles;
    exports net.fuxle.awooapi.server.intf.handler.common;
}
