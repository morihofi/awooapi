package net.fuxle.awooapi.component.scheduler.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// Annotation to define a cron job
@Retention(RetentionPolicy.RUNTIME)
public @interface CronJob {
    String expression(); // Crontab expression
    String name();       // Job name
    boolean runOnAppStart() default false; // Run on application start
}
