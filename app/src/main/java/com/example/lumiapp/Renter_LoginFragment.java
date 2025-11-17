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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Renter_LoginFragment extends Fragment {

    // All variables from your original LoginActivity
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvNoAccount;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public Renter_LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // NOTE: This inflates fragment_renter_login.xml. For now, you can have this XML be a copy of fragment_pm_login.xml.
        return inflater.inflate(R.layout.fragment_renter_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find all views using `view.findViewById()`
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.signup_Pass); // Make sure this ID is correct in your XML
        btnLogin = view.findViewById(R.id.btnLogin);
        tvNoAccount = view.findViewById(R.id.tvNoAccount);

        // Set click listeners
        btnLogin.setOnClickListener(v -> tryLogin());

        // This is the trigger to switch back to the Signup fragment
        tvNoAccount.setOnClickListener(v -> {
            if (getParentFragment() instanceof AuthFragmentSwitcher) {
                ((AuthFragmentSwitcher) getParentFragment()).switchToSignup();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
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

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(getActivity(), "Renter login successful!", Toast.LENGTH_SHORT).show();
                    routeAfterAuth();
                })
                .addOnFailureListener(e -> showError(e))
                .addOnCompleteListener(task -> toggleLoading(false));
    }

    // This method is identical to the one in SignupFragment
    private void routeAfterAuth() {

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    String userType = doc.getString("userType");

                    if (doc.getString("userType") == null || !doc.exists()) {
                        Toast.makeText(getActivity(), "User profile not found.", Toast.LENGTH_LONG).show();
                        auth.signOut();
                        goToAuthActivity();
                        return;
                    }

                    if ("renter".equals(userType)) {
                        // This is a Renter, and they are in the correct activity. We stay here.
                    } else if ("manager".equals(userType)) {
                        // This is a PM who has incorrectly landed here. Redirect them.
                        goToPropertyManagerDashboard();
                    } else {
                        // Unknown user role.
                        Toast.makeText(getActivity(), "User role could not be determined.", Toast.LENGTH_LONG).show();
                        auth.signOut();
                        goToAuthActivity();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to load profile. Directing to main dashboard.", Toast.LENGTH_SHORT).show();
                    goToDashboard();
                });
    }

    private void goToDashboard() {
        if (getActivity() == null) return;
        Intent i = new Intent(getActivity(), RenterMainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        getActivity().finish();
    }

    private void goToPropertyManagerDashboard() {
        Intent i = new Intent(getActivity(), PMDashboardContainer.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        getActivity().finish();
    }

    private void goToAuthActivity() {
        Intent i = new Intent(getActivity(), AuthActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        getActivity().finish();
    }

    private void toggleLoading(boolean loading) {
        if (btnLogin != null) {
            btnLogin.setEnabled(!loading);
            btnLogin.setText(loading ? "Logging in..." : "Login");
        }
    }

    private void clearErrors() {
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPassword != null) tilPassword.setError(null);
    }

    private String str(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void showError(Exception e) {
        String msg = "Login failed. Please check your credentials.";
        if (e != null && e.getMessage() != null && e.getMessage().contains("password")) {
            msg = "Incorrect password.";
        }
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}
