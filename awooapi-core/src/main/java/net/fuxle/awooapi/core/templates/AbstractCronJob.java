package net.fuxle.awooapi.core.templates;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public abstract class AbstractCronJob {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * This method should contain the main logic of the cron job.
     * Subclasses must implement this method to define job-specific behavior.
     */
    public abstract void run();

    /**
     * Optional method that runs before the job starts.
     * Subclasses can override this to perform setup tasks.
     */
    public void beforeRun() {
        // Default implementation: do nothing
    }

    /**
     * Optional method that runs after the job completes.
     * Subclasses can override this to perform cleanup tasks.
     */
    public void afterRun() {
        // Default implementation: do nothing
    }

    /**
     * Executes the cron job with built-in error handling and hooks.
     */
    public final void execute() {
        try {
            logger.info("Starting cron job: {}", getClass().getName());
            beforeRun();
            run();
            afterRun();
            logger.info("Finished cron job: {}", getClass().getName());
        } catch (Exception e) {
            logger.error("Error occurred during execution of cron job", e);
        }
    }
}
