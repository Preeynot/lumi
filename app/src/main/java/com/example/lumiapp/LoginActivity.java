package com.example.lumiapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvNoAccount;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Connect the activity to its layout file
        setContentView(R.layout.activity_login);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find views from the layout
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.signup_Pass); // Make sure this ID matches your activity_login.xml
        btnLogin = findViewById(R.id.btnLogin);
        tvNoAccount = findViewById(R.id.tvNoAccount);

        // Set click listeners
        btnLogin.setOnClickListener(v -> tryLogin());

        tvNoAccount.setOnClickListener(v -> {
            // Navigate back to the SignupActivity
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            // Optional: finish() this activity if you don't want it on the back stack
            //finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If the user is already logged in, route them immediately
        if (auth.getCurrentUser() != null) {
            routeAfterAuth();
        }
    }

    private void tryLogin() {
        clearErrors();

        String email = str(etEmail);
        String pass = str(etPassword);

        boolean ok = true;
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.invalid_email));
            ok = false;
        }
        if (TextUtils.isEmpty(pass)) {
            tilPassword.setError(getString(R.string.required));
            ok = false;
        }
        if (!ok) return;

        toggleLoading(true);

        // Sign in the user with Firebase Auth
        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    // Login was successful, now check the user's profile to route them
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    routeAfterAuth();
                })
                .addOnFailureListener(e -> {
                    // Login failed, show an error message
                    showError(e);
                })
                .addOnCompleteListener(task -> {
                    // This runs after success or failure, so we can stop the loading indicator here
                    toggleLoading(false);
                });
    }

    /**
     * Decides where to go next: PM setup (if incomplete) or the main dashboard.
     * This method is identical to the one in SignupActivity for consistency.
     */
    private void routeAfterAuth() {
        if (auth.getCurrentUser() == null) {
            // This case should ideally not happen after a successful login, but it's a good safeguard
            return;
        }
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    // Check the 'pmCompleted' flag in the user's Firestore document
                    boolean pmCompleted = doc != null && Boolean.TRUE.equals(doc.getBoolean("pmCompleted"));
                    if (pmCompleted) {
                        goToDashboard(); // If setup is complete, go to the main app
                    } else {
                        goToPMSetup();   // If setup is not complete, go to the setup screen
                    }
                })
                .addOnFailureListener(e -> {
                    // If we can’t read the user's document, default to the setup screen to be safe
                    Toast.makeText(this, "Failed to load profile. Directing to setup.", Toast.LENGTH_SHORT).show();
                    goToPMSetup();
                });
    }

    private void goToPMSetup() {
        Intent i = new Intent(LoginActivity.this, PMAccSetup.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish(); // Finish this activity so the user can't go back to it
    }

    private void goToDashboard() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish(); // Finish this activity so the user can't go back to it
    }

    private void toggleLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? getString(R.string.logging_in) : getString(R.string.login));
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    // Helper method to get trimmed string from an EditText
    private String str(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    // Helper method to show a generic error toast
    private void showError(Exception e) {
        String msg = "Login failed. Please check your credentials.";
        if (e != null && e.getMessage() != null && e.getMessage().contains("password")) {
            msg = "Incorrect password.";
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
