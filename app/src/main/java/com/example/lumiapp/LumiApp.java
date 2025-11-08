package com.example.lumiapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class LumiApp extends Application {
    public static final String CHAN_OPS  = "lumi_ops";   // For urgent alerts like doorbell
    public static final String CHAN_INFO = "lumi_info";  // For general info

    @Override
    public void onCreate() {
        super.onCreate();
        // Notification channels are required for Android 8.0 (Oreo) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);

            // High importance channel for operational alerts
            NotificationChannel ops = new NotificationChannel(CHAN_OPS, "Operational Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            ops.setDescription("For urgent notifications like tickets and doorbell calls.");

            // Default importance channel for general information
            NotificationChannel info = new NotificationChannel(CHAN_INFO, "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            info.setDescription("For general app information and updates.");

            if (nm != null) {
                nm.createNotificationChannel(ops);
                nm.createNotificationChannel(info);
            }
        }
    }
}
