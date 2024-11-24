package net.fuxle.awooapi.component.scheduler;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.*;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import net.fuxle.awooapi.common.plugin.impl.PluginEnvironment;
import net.fuxle.awooapi.common.plugin.intf.AbstractPlugin;
import net.fuxle.awooapi.component.scheduler.annotation.CronJob;
import net.fuxle.awooapi.component.scheduler.intf.AbstractCronJob;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZonedDateTime;

public class CronJobPlugin extends AbstractPlugin {
    private final ScheduledExecutorService scheduler;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>(); // Track scheduled jobs

    private static final Map<String, String> PREDEFINED_PATTERN = Map.of(
            "@hourly", "0 * * * *",
            "@daily", "0 0 * * *",
            "@weekly", "0 0 * * 0",
            "@monthly", "0 0 1 * *",
            "@yearly", "0 0 1 1 *",
            "@annually", "0 0 1 1 *" // Same as @yearly
    );

    public CronJobPlugin(PluginEnvironment env) {
        super(env);
        // Extract Parameters from Map
        this.scheduler = Executors.newScheduledThreadPool((Integer) env.getParameter().get(CronJobPluginConfig.THREAD_POOL_SIZE));
    }

    public void discoverAndStartJobs(String packageName, Reflections reflections) {
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
                    scheduler.submit(jobInstance::execute);
                }

                // Schedule the job based on the cron expression
                addJob(annotation.expression(), jobInstance);

            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate CronJob class: " + clazz.getName(), e);
            }
        }
    }

    public void addJob(String expression, AbstractCronJob jobInstance) {
        try {
            long interval = parseCronExpressionToMillis(expression);
            ZonedDateTime nextExecutionTime = ZonedDateTime.now().plus(Duration.ofMillis(interval));
            log.info("Next cronjob execution for class {} scheduled at {} ", jobInstance.getClass().getName(), nextExecutionTime);

            // Generate a unique identifier for each job instance
            String jobKey = jobInstance.getClass().getName() + "-" + UUID.randomUUID();

            // Schedule the job with a delay to avoid overlapping executions
            ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    jobInstance.run();
                } catch (Exception e) {
                    log.error("Exception occurred while running cron job: {}", jobInstance.getClass().getName(), e);
                }
            }, 0, interval, TimeUnit.MILLISECONDS);

            // Store the scheduled job using its unique key
            scheduledJobs.put(jobKey, scheduledFuture);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cron expression: " + expression, e);
        }
    }

    public void removeJob(String jobClassName) {
        // Remove all jobs that match the given class name (since each instance now has a unique identifier)
        List<String> keysToRemove = new ArrayList<>();
        for (String key : scheduledJobs.keySet()) {
            if (key.startsWith(jobClassName)) {
                keysToRemove.add(key);
            }
        }

        for (String key : keysToRemove) {
            ScheduledFuture<?> scheduledFuture = scheduledJobs.get(key);
            if (scheduledFuture != null) {
                boolean cancelled = scheduledFuture.cancel(false);
                if (cancelled) {
                    log.info("Successfully cancelled cron job: {}", key);
                    scheduledJobs.remove(key);
                } else {
                    log.warn("Failed to cancel cron job: {}", key);
                }
            }
        }

        if (keysToRemove.isEmpty()) {
            log.warn("No cron job found with class name: {}", jobClassName);
        }
    }

    long parseCronExpressionToMillis(String expression) {
        // Handle predefined cron expressions
        String expandedExpression = PREDEFINED_PATTERN.getOrDefault(expression, expression);

        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        Cron cron = parser.parse(expandedExpression);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        Optional<Duration> interval;
        try {
            interval = executionTime.timeToNextExecution(ZonedDateTime.now());
        } catch (Exception e) {
            log.error("Error calculating interval for cron expression '{}': {}", expression, e.getMessage());
            throw new IllegalArgumentException("Unable to calculate interval for cron expression", e);
        }

        return interval.orElseThrow(() -> new IllegalArgumentException("Unable to calculate interval for cron expression: " + expression))
                .toMillis();
    }


    @Override
    public void initialize() {
        log.info("Cronjob Plugin has been initialized");
    }

    @Override
    public void unload() {
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
