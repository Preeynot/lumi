package com.example.lumiapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Renter_SignupFragment extends Fragment {

    // All variables from your original SignupActivity
    private TextInputLayout tilName, tilEmail, tilPhone, tilPassword;
    private TextInputEditText etName, etEmail, etPhone, etPassword;
    private MaterialButton btnCreate;
    private TextView tvAlready;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public Renter_SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // NOTE: This inflates fragment_renter_signup.xml. For now, you can have this XML be a copy of fragment_pm_signup.xml.
        return inflater.inflate(R.layout.fragment_renter_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find all views using `view.findViewById()`
        tilName = view.findViewById(R.id.tilName);
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPhone = view.findViewById(R.id.tilPhone);
        tilPassword = view.findViewById(R.id.tilPassword);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.signup_Phone);
        etPassword = view.findViewById(R.id.signup_Pass);
        btnCreate = view.findViewById(R.id.btnCreate);
        tvAlready = view.findViewById(R.id.tvAlready);

        // Set click listeners
        btnCreate.setOnClickListener(v -> tryCreateAccount());

        // This is the trigger to switch to the Login fragment
        tvAlready.setOnClickListener(v -> {
            if (getParentFragment() instanceof AuthFragmentSwitcher) {
                ((AuthFragmentSwitcher) getParentFragment()).switchToLogin();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // If a user is already logged in, route them immediately
        if (auth.getCurrentUser() != null) {
            routeAfterAuth();
        }
    }

    private void tryCreateAccount() {
        clearErrors();
        String name = str(etName);
        String email = str(etEmail);
        String phone = str(etPhone);
        String pass = str(etPassword);

        boolean ok = true;
        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.required));
            ok = false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.invalid_email));
            ok = false;
        }
        if (TextUtils.isEmpty(phone) || phone.length() < 7) {
            tilPhone.setError(getString(R.string.invalid_phone));
            ok = false;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            tilPassword.setError(getString(R.string.password_min_chars));
            ok = false;
        }
        if (!ok) return;

        toggleLoading(true);

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(getActivity(), "Renter account created!", Toast.LENGTH_SHORT).show();
                    if (auth.getCurrentUser() == null) {
                        toggleLoading(false);
                        return;
                    }
                    saveUserProfile(name, email, phone);
                })
                .addOnFailureListener(e -> {
                    toggleLoading(false);
                    showError(e);
                });
    }

    private void saveUserProfile(String name, String email, String phone) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
        auth.getCurrentUser().updateProfile(profileUpdates)
                .addOnSuccessListener(aVoid -> {
                    String uid = auth.getCurrentUser().getUid();
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("uid", uid);
                    profile.put("name", name);
                    profile.put("email", email);
                    profile.put("phone", phone);
                    profile.put("createdAt", Timestamp.now());
                    profile.put("userType", "renter"); // ✅ KEY CHANGE: Differentiate user type as "renter"
                    // Add any renter-specific flags here, e.g.,
                    // profile.put("renterSetupCompleted", false);

                    db.collection("users").document(uid).set(profile)
                            .addOnSuccessListener(unused -> routeAfterAuth())
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Profile save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                routeAfterAuth();
                            })
                            .addOnCompleteListener(task -> toggleLoading(false));
                })
                .addOnFailureListener(e -> {
                    toggleLoading(false);
                    Toast.makeText(getActivity(), "Profile update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    routeAfterAuth();
                });
    }

    private void routeAfterAuth() {
        // For now, this can route to the same places. You will change this later.
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    String userType = doc.getString("userType");

                    // TODO: Create a Renter-specific dashboard/setup later.
                    if ("renter".equals(userType)) {
                        // For now, go to the same PM dashboard for testing.
                        goToDashboard();
                    } else {
                        // Default fallback
                        goToDashboard();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to read profile, routing to main dashboard.", Toast.LENGTH_SHORT).show();
                    goToDashboard();
                });
    }

    private void goToDashboard() {
        // TODO: This should eventually go to a RenterMainActivity
        if (getActivity() == null) return;
        Intent i = new Intent(getActivity(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        getActivity().finish();
    }

    private void toggleLoading(boolean loading) {
        if (btnCreate != null) {
            btnCreate.setEnabled(!loading);
            btnCreate.setText(loading ? "Creating..." : "Create Account");
        }
    }

    private void clearErrors() {
        if (tilName != null) tilName.setError(null);
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPhone != null) tilPhone.setError(null);
        if (tilPassword != null) tilPassword.setError(null);
    }

    private String str(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void showError(Exception e) {
        String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "Signup failed";
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}
