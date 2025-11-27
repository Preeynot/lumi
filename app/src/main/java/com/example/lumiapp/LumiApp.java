package com.example.lumiapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.LocalCacheSettings;
import com.google.firebase.firestore.PersistentCacheSettings;
import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory;
import io.getstream.chat.android.state.plugin.config.StatePluginConfig;
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory;


public class LumiApp extends Application {
    public static final String CHAN_OPS  = "lumi_ops";   // For urgent alerts like doorbell
    public static final String CHAN_INFO = "lumi_info";  // For general info

    @Override
    public void onCreate() {
        super.onCreate();

        // --- Setup Stream Chat Client ---
        // The offline plugin stores messages locally, allowing the app to work offline.
        StreamOfflinePluginFactory offlinePluginFactory = new StreamOfflinePluginFactory(getApplicationContext());
        // The state plugin handles real-time updates and state management.
        StreamStatePluginFactory statePluginFactory = new StreamStatePluginFactory(new StatePluginConfig(),getApplicationContext());

        // Initialize the ChatClient with your Stream API Key.
        ChatClient client = new ChatClient.Builder("YOUR_STREAM_API_KEY", getApplicationContext())
                .withPlugins(offlinePluginFactory, statePluginFactory)
                .logLevel(ChatLogLevel.ALL) // Use ALL for debugging, ERROR for production
                .build();
        // ---------------------------------

        // --- Your existing Notification Channel code ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);

            NotificationChannel ops = new NotificationChannel(CHAN_OPS, "Operational Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            ops.setDescription("For urgent notifications like tickets and doorbell calls.");

            android.app.NotificationChannel info = new NotificationChannel(CHAN_INFO, "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            info.setDescription("For general app information and updates.");
            if (nm != null) {
                nm.createNotificationChannel(ops);
                nm.createNotificationChannel(info);
            }
        }
        // Enable Firestore offline persistence
        PersistentCacheSettings persistentCacheSettings = PersistentCacheSettings.newBuilder().build();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(persistentCacheSettings)
                .build();
        db.setFirestoreSettings(settings);

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
