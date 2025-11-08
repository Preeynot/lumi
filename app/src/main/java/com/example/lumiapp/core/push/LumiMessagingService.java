package com.example.lumiapp.core.push;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.lumiapp.LumiApp;
import com.example.lumiapp.R;
import com.example.lumiapp.features.video.CallActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class LumiMessagingService extends FirebaseMessagingService {
    // This is called when a new FCM token is generated for the device
    @Override
    public void onNewToken(@NonNull String token) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                    .update("fcmToken", token);
        }
    }

    // This is called when a push notification is received while the app is in the foreground or background
    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        String type = msg.getData() != null ? msg.getData().get("type") : null;

        if ("DOORBELL_CALL".equals(type)) {
            String channel = msg.getData().get("channel");
            handleDoorbellCall(channel);
        }
    }

    private void handleDoorbellCall(String channelName) {
        if (channelName == null || channelName.isEmpty()) return;

        // Create an intent that will open the CallActivity when the notification is tapped
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("channelName", channelName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, LumiApp.CHAN_OPS)
                .setSmallIcon(R.drawable.ic_stat_lumi)
                .setContentTitle("Doorbell")
                .setContentText("A visitor is at the door. Tap to answer.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
