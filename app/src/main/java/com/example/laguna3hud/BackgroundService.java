package com.example.laguna3hud;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class BackgroundService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(
                () -> {
                    while (true) {
                        Log.i("Service", "The service is still runing in the background...");
                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();
        return super.onStartCommand(intent, flags, startId);
    }

        @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
