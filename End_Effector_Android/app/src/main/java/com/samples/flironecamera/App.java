package com.samples.flironecamera;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {
    public ExecutorService executorService = Executors.newFixedThreadPool(4);
    public Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
}
