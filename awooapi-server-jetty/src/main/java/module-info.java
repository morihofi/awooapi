module awooapi.server.jetty {
    requires org.slf4j;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.ee10.servlet;
    requires awooapi.server.intf;
    requires awooapi.annotations;
}
