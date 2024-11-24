package net.fuxle.awooapi.component.scheduler;

import java.util.HashMap;
import java.util.Map;

public class CronJobPluginConfig {
    private final Map<String,Object> parameter = new HashMap<>();

    public static String THREAD_POOL_SIZE = "scheduler.threadpool.size";

    public CronJobPluginConfig(int threadPoolSize) {
        parameter.put(THREAD_POOL_SIZE, threadPoolSize);
    }

    public Map<String, Object> getParameter() {
        return parameter;
    }
}
