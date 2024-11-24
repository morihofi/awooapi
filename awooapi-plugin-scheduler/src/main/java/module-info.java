module awooapi.plugin.scheduler {
    requires com.cronutils;
    requires org.reflections;
    requires awooapi.plugin.intf;
    requires org.slf4j;

    exports net.fuxle.awooapi.component.scheduler.intf;
}