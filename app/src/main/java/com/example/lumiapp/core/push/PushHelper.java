package com.example.lumiapp.core.push;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class PushHelper {
    // Call this after a user logs in or signs up
    public static void saveTokenForCurrentUser() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token ->
                        FirebaseFirestore.getInstance().collection("users")
                                .document(uid).update("fcmToken", token));
    }

    // Call this when a PM creates a property, or a Renter joins one
    public static void subscribeToPropertyTopic(String propertyId) {
        if (propertyId == null || propertyId.isEmpty()) return;
        // Topics allow sending a message to multiple devices at once
        FirebaseMessaging.getInstance().subscribeToTopic("property_" + propertyId);
    }
}
