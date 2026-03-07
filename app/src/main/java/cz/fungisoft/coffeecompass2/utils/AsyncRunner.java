package cz.fungisoft.coffeecompass2.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple executor helper to replace AsyncTask usage.
 */
public final class AsyncRunner {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private AsyncRunner() {
    }

    public static void runInBackground(Runnable task) {
        EXECUTOR.execute(task);
    }

    public static void runOnMainThread(Runnable task) {
        MAIN_HANDLER.post(task);
    }
}
