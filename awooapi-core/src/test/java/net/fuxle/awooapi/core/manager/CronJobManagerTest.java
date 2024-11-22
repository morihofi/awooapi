package net.fuxle.awooapi.core.manager;

import net.fuxle.awooapi.core.templates.AbstractCronJob;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CronJobManagerTest {

    @Test
    void testValidCronExpression() {
        CronJobManager manager = new CronJobManager();

        // Test with a valid cron expression
        String validExpression = "*/5 * * * *"; // Every 5 minutes
        long interval = manager.parseCronExpressionToMillis(validExpression);

        // Verify the interval is within the expected range (less than or equal to 5 minutes)
        long expectedMaxInterval = Duration.ofMinutes(5).toMillis();
        assertTrue(interval <= expectedMaxInterval && interval > 0,
                "Interval should be less than or equal to 5 minutes and greater than 0. Actual: " + interval);
    }

    @Test
    void testInvalidCronExpression() {
        CronJobManager manager = new CronJobManager();

        // Test with an invalid cron expression
        String invalidExpression = "invalid";
        assertThrows(IllegalArgumentException.class, () -> manager.parseCronExpressionToMillis(invalidExpression));
    }

    @Test
    void testHourlyCronExpression() {
        CronJobManager manager = new CronJobManager();

        // Test hourly cron expression
        String hourlyExpression = "0 * * * *"; // At the top of every hour
        long interval = manager.parseCronExpressionToMillis(hourlyExpression);

        // Verify the interval is less than or equal to 1 hour and greater than 0
        long expectedMaxInterval = Duration.ofHours(1).toMillis();
        assertTrue(interval <= expectedMaxInterval && interval > 0,
                "Interval should be less than or equal to 1 hour and greater than 0. Actual: " + interval);
    }

    @Test
    void testPredefinedCronExpression() {
        CronJobManager manager = new CronJobManager();

        // Test predefined @hourly cron expression
        String hourlyExpression = "@hourly";
        long interval = manager.parseCronExpressionToMillis(hourlyExpression);

        // Verify the interval is less than or equal to 1 hour and greater than 0
        long expectedMaxInterval = Duration.ofHours(1).toMillis();
        assertTrue(interval <= expectedMaxInterval && interval > 0,
                "Interval should be less than or equal to 1 hour and greater than 0. Actual: " + interval);
    }

    @Test
    void testJobScheduling() {
        CronJobManager manager = new CronJobManager();

        // Use a CountDownLatch to track job execution
        CountDownLatch latch = new CountDownLatch(1);

        // Mock Job
        AbstractCronJob mockJob = new AbstractCronJob() {
            @Override
            public void run() {
                latch.countDown(); // Signal job execution
            }
        };

        // Schedule the job for every minute
        String cronExpression = "*/1 * * * *";
        manager.scheduleJob(cronExpression, mockJob);

        try {
            // Wait for the job to execute within a timeout of 70 seconds
            boolean executed = latch.await(70, TimeUnit.SECONDS);
            assertTrue(executed, "Job was not executed within the expected time.");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }
}
