package com.example.lumiapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    // Declare the BottomNavigationView
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_main); // simple splash/progress layout
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // Apply your Material theme
        setTheme(R.style.AppTheme);

        // Inflate your main layout file (activity_main.xml)
        setContentView(R.layout.activity_main);

        // Connect the Java variable to the BottomNavigationView in XML
        bottomNav = findViewById(R.id.bottom_nav);

        // Set which item is selected when the app launches
        bottomNav.setSelectedItemId(R.id.nav_home);

        // Handle tab changes (clicks on the bottom navigation icons)
        /*bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    // TODO: Replace with fragment or activity logic for Home
                    return true;

                case R.id.nav_property:
                    // TODO: Replace with fragment or activity logic for Property
                    return true;

                case R.id.nav_messages:
                    // TODO: Replace with fragment or activity logic for Messages
                    return true;

                case R.id.nav_profile:
                    // TODO: Replace with fragment or activity logic for Profile
                    return true;
            }
            // Return false if no valid item was clicked
            return false;
        });*/

        // ---------------------------
        // 🔔 Add Badges to Icons
        // ---------------------------

        // 1️⃣ Create a badge for the Messages tab
        BadgeDrawable msgBadge = bottomNav.getOrCreateBadge(R.id.nav_messages);
        msgBadge.setVisible(true);  // Show the badge
        msgBadge.setNumber(7);      // Set a numeric badge (like unread count)

        // 2️⃣ Create a badge for the Property tab (dot-only badge)
        BadgeDrawable propertyBadge = bottomNav.getOrCreateBadge(R.id.nav_property);
        propertyBadge.setVisible(true);
        propertyBadge.clearNumber(); // Removes number, leaving just a small dot

        // Optional: Adjust badge position (if it overlaps icon)
        // msgBadge.setHorizontalOffset(6);
        // msgBadge.setVerticalOffset(4);

        // Optional: Remove badge later (e.g., after messages are read)
        // bottomNav.removeBadge(R.id.nav_messages);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (auth.getCurrentUser() == null) {
            // Not signed in → back to signup
            Intent i = new Intent(this, SignupActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

        // Signed in → fetch user doc and decide
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(this::routeByUserDoc)
                .addOnFailureListener(e -> {
                    // If we can’t read, be safe and send to PM setup
                    Toast.makeText(this, "Loading profile failed, opening setup.", Toast.LENGTH_SHORT).show();
                    goToPMSetup();
                });
    }

    private void routeByUserDoc(DocumentSnapshot doc) {
        // For now: treat all as Property Manager
        boolean pmCompleted = doc != null && Boolean.TRUE.equals(doc.getBoolean("pmCompleted"));
        if (pmCompleted) {
            goToPMDashboard();
        } else {
            goToPMSetup();
        }
    }

    private void goToPMDashboard() {
        Intent i = new Intent(this, PMDashboardContainer.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void goToPMSetup() {
        Intent i = new Intent(this, PMAccSetup.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

}
