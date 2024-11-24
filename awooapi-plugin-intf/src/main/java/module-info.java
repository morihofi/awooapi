module awooapi.plugin.intf {
    requires org.slf4j;
    requires awooapi.server.intf;

    exports net.fuxle.awooapi.common.plugin.intf;
    exports net.fuxle.awooapi.common.plugin.impl;
    exports net.fuxle.awooapi.common.plugin;
}