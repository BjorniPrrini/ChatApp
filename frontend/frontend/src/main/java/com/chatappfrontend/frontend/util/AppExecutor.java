package com.chatappfrontend.frontend.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutor {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void run(Runnable task){
        executorService.submit(task);
    }
}