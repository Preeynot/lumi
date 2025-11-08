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

import com.example.lumiapp.core.push.PushHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Renter_SignupFragment extends Fragment {

    private TextInputLayout tilName, tilEmail, tilPhone, tilPassword, tilPropertyId, tilUnitId;
    private TextInputEditText etName, etEmail, etPhone, etPassword, etPropertyId, etUnitId;
    private MaterialButton btnCreate;
    private TextView tvAlready;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public Renter_SignupFragment() { /* Required empty public constructor */ }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_renter_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        initializeFirebase();
        setupClickListeners();
    }

    private void initializeViews(View view) {
        tilName = view.findViewById(R.id.tilName);
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPhone = view.findViewById(R.id.tilPhone);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilPropertyId = view.findViewById(R.id.tilPropertyId);
        tilUnitId = view.findViewById(R.id.tilUnitId);

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.signup_Phone);
        etPassword = view.findViewById(R.id.signup_Pass);
        etPropertyId = view.findViewById(R.id.etPropertyId);
        etUnitId = view.findViewById(R.id.etUnitId);

        btnCreate = view.findViewById(R.id.btnCreate);
        tvAlready = view.findViewById(R.id.tvAlready);
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setupClickListeners() {
        btnCreate.setOnClickListener(v -> tryCreateAccount());
        tvAlready.setOnClickListener(v -> {
            if (getParentFragment() instanceof AuthFragmentSwitcher) {
                ((AuthFragmentSwitcher) getParentFragment()).switchToLogin();
            }
        });
    }

    private void tryCreateAccount() {
        clearErrors();

        String name = str(etName);
        String email = str(etEmail);
        String pass = str(etPassword);
        String propertyId = str(etPropertyId);
        String unitId = str(etUnitId);

        boolean ok = true;
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Required"); ok = false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email"); ok = false;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            tilPassword.setError("Minimum 6 characters"); ok = false;
        }
        if (TextUtils.isEmpty(propertyId)) {
            tilPropertyId.setError("Required"); ok = false;
        }
        if (TextUtils.isEmpty(unitId)) {
            tilUnitId.setError("Required"); ok = false;
        }

        if (!ok) return;

        toggleLoading(true);

        // Verify the unit exists BEFORE creating the user
        db.collection("units").document(unitId).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists() || !propertyId.equals(snap.getString("propertyId"))) {
                        Toast.makeText(getActivity(), "Unit not found or does not belong to the specified Property Code.", Toast.LENGTH_LONG).show();
                        toggleLoading(false);
                        return;
                    }
                    // Unit is valid, now create the user
                    auth.createUserWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(authResult -> {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user == null) {
                                    toggleLoading(false);
                                    return;
                                }
                                saveUserProfileAndAttachToUnit(user, name, email, str(etPhone), propertyId, unitId);
                            })
                            .addOnFailureListener(e -> {
                                toggleLoading(false);
                                showError(e);
                            });
                })
                .addOnFailureListener(e -> {
                    toggleLoading(false);
                    showError(e);
                });
    }

    private void saveUserProfileAndAttachToUnit(FirebaseUser user, String name, String email, String phone, String propertyId, String unitId) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    String uid = user.getUid();

                    // Create user profile
                    Map<String, Object> userProfile = new HashMap<>();
                    userProfile.put("uid", uid);
                    userProfile.put("name", name);
                    userProfile.put("email", email);
                    userProfile.put("phone", phone);
                    userProfile.put("createdAt", Timestamp.now());
                    userProfile.put("userType", "renter");
                    userProfile.put("propertyIds", FieldValue.arrayUnion(propertyId));

                    db.collection("users").document(uid).set(userProfile)
                            .addOnSuccessListener(aVoid -> {
                                // Attach tenant to unit
                                db.collection("units").document(unitId).update("tenantIds", FieldValue.arrayUnion(uid))
                                        .addOnSuccessListener(v -> {
                                            PushHelper.saveTokenForCurrentUser();
                                            PushHelper.subscribeToPropertyTopic(propertyId);
                                            routeToDashboard();
                                        })
                                        .addOnFailureListener(this::showError);
                            })
                            .addOnFailureListener(this::showError)
                            .addOnCompleteListener(done -> toggleLoading(false));
                });
    }

    private void routeToDashboard() {
        if (getActivity() == null) return;
        // TODO: Create a RenterMainActivity and route here instead. For now, we go to the main one.
        Intent i = new Intent(getActivity(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        getActivity().finish();
    }

    // --- Helper Methods ---
    private void toggleLoading(boolean loading) {
        if (btnCreate != null) {
            btnCreate.setEnabled(!loading);
            btnCreate.setText(loading ? "Creating..." : "Create Account");
        }
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        if (tilPropertyId != null) tilPropertyId.setError(null);
        if (tilUnitId != null) tilUnitId.setError(null);
    }

    private String str(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void showError(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage() : "An unknown error occurred.";
        Toast.makeText(getActivity(), "Error: " + msg, Toast.LENGTH_LONG).show();
    }
}
