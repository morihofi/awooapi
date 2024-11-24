package net.fuxle.awooapi.component.scheduler;

import net.fuxle.awooapi.component.scheduler.intf.AbstractCronJob;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CronJobManagerTest {

    @Test
    void testValidCronExpression() {
        CronJobPlugin manager = new CronJobPlugin(null);

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
        CronJobPlugin manager = new CronJobPlugin(null);

        // Test with an invalid cron expression
        String invalidExpression = "invalid";
        assertThrows(IllegalArgumentException.class, () -> manager.parseCronExpressionToMillis(invalidExpression),
                "Expected IllegalArgumentException for invalid cron expression.");
    }

    @Test
    void testHourlyCronExpression() {
        CronJobPlugin manager = new CronJobPlugin(null);

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
        CronJobPlugin manager = new CronJobPlugin(null);

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
        CronJobPlugin manager = new CronJobPlugin(null);

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
        manager.addJob(cronExpression, mockJob);

        try {
            // Wait for the job to execute within a timeout of 70 seconds
            boolean executed = latch.await(70, TimeUnit.SECONDS);
            assertTrue(executed, "Job was not executed within the expected time.");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }


    @Test
    void testMultipleJobScheduling() {
        CronJobPlugin manager = new CronJobPlugin(null);

        // Use a CountDownLatch to track multiple job executions
        int numberOfJobs = 3;
        CountDownLatch latch = new CountDownLatch(numberOfJobs);

        // Schedule multiple jobs
        for (int i = 0; i < numberOfJobs; i++) {
            AbstractCronJob mockJob = new AbstractCronJob() {
                @Override
                public void run() {
                    latch.countDown(); // Signal job execution
                }
            };

            String cronExpression = "*/1 * * * *";
            manager.addJob(cronExpression, mockJob);
        }

        try {
            // Wait for all jobs to execute within a timeout of 70 seconds
            boolean allExecuted = latch.await(70, TimeUnit.SECONDS);
            assertTrue(allExecuted, "Not all jobs were executed within the expected time.");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }
}