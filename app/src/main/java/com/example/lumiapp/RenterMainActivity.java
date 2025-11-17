package com.example.lumiapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RenterMainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    // Declare the BottomNavigationView
    private BottomNavigationView renterBottomNav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Inflate your main layout file (activity_renter_main.xml)
        setContentView(R.layout.activity_renter_main);
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // Apply your Material theme
        setTheme(R.style.AppTheme);

        // Connect the Java variable to the BottomNavigationView in XML
        renterBottomNav = findViewById(R.id.bottom_nav);

        // Set which item is selected when the app launches
        renterBottomNav.setSelectedItemId(R.id.nav_home);

        // Handle tab changes (clicks on the bottom navigation icons)
        renterBottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_renter_home) {
               // selectedFragment = new DashboardRenter();
            } else if (itemId == R.id.nav_renter_property) {
                //selectedFragment = new Renter_PropertiesFragment(); // You will create this fragment
            } else if (itemId == R.id.nav_renter_messages) {
                //selectedFragment = new Shared_MessagesFragment(); // Example of a shared fragment
            } else if (itemId == R.id.nav_renter_profile) {
                //selectedFragment = new Shared_MessagesFragment(); // Example of a shared fragment
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main, selectedFragment)
                        .commit();
            }
            return true;
        });
        // ---------------------------
        // 🔔 Add Badges to Icons
        // ---------------------------

        // 1️⃣ Create a badge for the Messages tab
        BadgeDrawable msgBadge = renterBottomNav.getOrCreateBadge(R.id.nav_messages);
        msgBadge.setVisible(true);  // Show the badge
        msgBadge.setNumber(7);      // Set a numeric badge (like unread count)

        // 2️⃣ Create a badge for the Property tab (dot-only badge)
        BadgeDrawable propertyBadge = renterBottomNav.getOrCreateBadge(R.id.nav_property);
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
            Intent i = new Intent(this, AuthActivity.class);
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
                    // If we can’t read, be safe and send to Renter setup
                    Toast.makeText(this, "Loading profile failed, opening setup.", Toast.LENGTH_SHORT).show();
                    goToRenterSetup();
                });
    }

    private void routeByUserDoc(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            // The user is authenticated, but their profile document is missing in Firestore.
            Toast.makeText(this, "User profile data not found. Please contact support.", Toast.LENGTH_LONG).show();
            auth.signOut();
            goToAuthActivity();
            return;
        }

        // Read the 'userType' field from the document.
        String userType = doc.getString("userType");

        if ("renter".equals(userType)) {
            // --- This is a Renter ---
            // Now, we can check if their specific setup is complete.
            boolean renterCompleted = Boolean.TRUE.equals(doc.getBoolean("renterCompleted"));
            if (renterCompleted) {
                // The Renter's setup is complete. Since we are ALREADY in the correct activity
                // (PropertyManagerMainActivity), we do nothing but stay here and load content.
                Toast.makeText(this, "Welcome Back, Tenant!", Toast.LENGTH_SHORT).show();
            } else {
                // The Renter has not completed their specific setup flow (e.g., creating a property).
                goToRenterSetup();
            }

        } else if ("manager".equals(userType)) {
            // --- This is a Property Manager ---
            // A Manager has incorrectly landed in the Renter's main activity.
            // Immediately redirect them to their correct home screen.
            goToPMDashboard();

        } else {
            // The user has an unknown or missing role. This is an error state.
            Toast.makeText(this, "User role could not be determined.", Toast.LENGTH_LONG).show();
            auth.signOut();
            goToAuthActivity();
        }
        }

    private void goToRenterSetup() {
        Intent i = new Intent(this, RenterAccSetup.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void goToAuthActivity() {
        Intent i = new Intent(this, AuthActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void goToPMDashboard() {
        Intent i = new Intent(this, PropertyManagerMainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

}
