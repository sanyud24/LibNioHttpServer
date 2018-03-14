package com.example.zjt.nioserver.thread;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by zjt on 18-1-31.
 */

public class ThreadPool {
    private static ExecutorService executorService;

    public static Future execute(Runnable runnable) {
        if (executorService == null) {
            int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors(),
                    KEEP_ALIVE_TIME = 1;
            TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
            BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
            executorService = new ThreadPoolExecutor(NUMBER_OF_CORES,
                    NUMBER_OF_CORES * 2,
                    KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, taskQueue);
        }
        return executorService.submit(runnable);
    }

    public static void shutdown() {
        if (executorService != null)
            executorService.shutdown();
    }
}
