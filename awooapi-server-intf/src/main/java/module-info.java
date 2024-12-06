module awooapi.server.intf {
    requires awooapi.annotations;
    requires org.slf4j;
    requires com.google.gson;
    exports net.fuxle.awooapi.server.intf;
    exports net.fuxle.awooapi.server.intf.handler;
    exports net.fuxle.awooapi.server.intf.handler.staticfiles;
    exports net.fuxle.awooapi.server.intf.handler.common;
    exports net.fuxle.awooapi.server.common.mozillasslconfig;
    exports net.fuxle.awooapi.server.common;
}
