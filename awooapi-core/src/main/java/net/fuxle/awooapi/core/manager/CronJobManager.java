package net.fuxle.awooapi.core.manager;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.Executors;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import net.fuxle.awooapi.annotations.CronJob;
import net.fuxle.awooapi.core.templates.AbstractCronJob;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CronJobManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5); // Configurable pool size
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Map<String, String> PREDEFINED_CRONS = Map.of(
            "@hourly", "0 * * * *",
            "@daily", "0 0 * * *",
            "@weekly", "0 0 * * 0",
            "@monthly", "0 0 1 * *",
            "@yearly", "0 0 1 1 *",
            "@annually", "0 0 1 1 *" // Same as @yearly
    );


    public void discoverAndStartJobs(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> cronJobClasses = reflections.getTypesAnnotatedWith(CronJob.class);

        for (Class<?> clazz : cronJobClasses) {
            CronJob annotation = clazz.getAnnotation(CronJob.class);

            if (!AbstractCronJob.class.isAssignableFrom(clazz)) {
                throw new IllegalStateException(
                        "Class " + clazz.getName() + " must extend " + AbstractCronJob.class.getName() + " to use @CronJob annotation."
                );
            }

            try {
                AbstractCronJob jobInstance = (AbstractCronJob) clazz.getDeclaredConstructor().newInstance();

                if (annotation.runOnAppStart()) {
                    // Submitting the job to run immediately on application start
                    scheduler.submit(jobInstance::run);
                }

                // Schedule the job based on the cron expression
                scheduleJob(annotation.expression(), jobInstance);

            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate CronJob class: " + clazz.getName(), e);
            }
        }
    }


    void scheduleJob(String expression, AbstractCronJob jobInstance) {
        try {
            long interval = parseCronExpressionToMillis(expression);
            ZonedDateTime nextExecutionTime = ZonedDateTime.now().plus(Duration.ofMillis(interval));
            log.info("Next cronjob execution for class {} scheduled at {} ", jobInstance.getClass().getName(), nextExecutionTime);

            // Schedule the job at a fixed rate with the calculated interval
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    jobInstance.run();
                } catch (Exception e) {
                    log.error("Exception occurred while running cron job: " + jobInstance.getClass().getName(), e);
                }
            }, 0, interval, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cron expression: " + expression, e);
        }
    }

    long parseCronExpressionToMillis(String expression) {
        // Handle predefined cron expressions
        String expandedExpression = PREDEFINED_CRONS.getOrDefault(expression, expression);

        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        Cron cron = parser.parse(expandedExpression);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        Optional<Duration> interval = executionTime.timeToNextExecution(ZonedDateTime.now());
        return interval.orElseThrow(() -> new IllegalArgumentException("Unable to calculate interval for cron expression"))
                .toMillis();
    }

    public void shutdown() {
        try {
            log.info("Shutting down scheduler...");
            scheduler.shutdown();
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("Scheduler did not terminate in the specified time.");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Shutdown interrupted.", e);
            scheduler.shutdownNow();
        }
    }

}
