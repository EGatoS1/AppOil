package com.example.oil_app.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reemplaza los "new Thread(() -> {...}).start()" que tenías repetidos
 * en cada Activity. Un solo executor para todo el proyecto.
 *
 * Uso:
 *   AppExecutors.getInstance().diskIO().execute(() -> { ...trabajo pesado... });
 *   AppExecutors.getInstance().mainThread().post(() -> { ...actualizar UI... });
 */
public class AppExecutors {

    private static AppExecutors instance;

    private final ExecutorService diskIO = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private AppExecutors() { }

    public static synchronized AppExecutors getInstance() {
        if (instance == null) {
            instance = new AppExecutors();
        }
        return instance;
    }

    public ExecutorService diskIO() {
        return diskIO;
    }

    public Handler mainThread() {
        return mainThreadHandler;
    }
}
