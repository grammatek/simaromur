package com.grammatek.simaromur.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class provides a simple way to run a task in a background thread and
 * then run a callback in the UI thread.
 *
 * reference:
 * https://stackoverflow.com/questions/4430922/how-to-run-a-task-in-background-and-update-the-ui
 * https://stackoverflow.com/questions/58767733/the-asynctask-api-is-deprecated-in-android-11-what-are-the-alternatives
 */
public abstract class AsyncThread {
    private final String LOG_TAG = "Simaromur_" + AsyncThread.class.getSimpleName();
    private final ExecutorService executors;
    private String threadName;

    public AsyncThread() {
        this.executors = Executors.newSingleThreadExecutor();
    }

    private void startBackground() {
        onPreExecute();
        executors.execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName(threadName);
                Log.v(LOG_TAG, threadName + ": before doInBackground()");
                doInBackground();
                Log.v(LOG_TAG, threadName + ": after doInBackground()");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.v(LOG_TAG, "UI: before onPostExecute()");
                        onPostExecute();
                        Log.v(LOG_TAG, "UI: after onPostExecute()");
                    }
                });
            }
        });
    }

    public void execute(String aThreadName) {
        threadName = aThreadName;
        Log.v(LOG_TAG, "UI: before startBackground()");
        startBackground();
        Log.v(LOG_TAG, "UI: after startBackground()");
    }

    public void shutdown() {
        executors.shutdown();
    }

    public boolean isShutdown() {
        return executors.isShutdown();
    }

    /**
     * Runs on the UI thread before doInBackground(Params...).
     */
    public abstract void onPreExecute();

    /**
     * This method is invoked on a background thread and should perform the long running task.
     */
    public abstract void doInBackground();

    /**
     * Runs on the UI thread after doInBackground(Params...).
     */
    public abstract void onPostExecute();
}
