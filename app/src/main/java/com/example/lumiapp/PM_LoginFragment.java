package com.example.lumiapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.models.User;
import io.getstream.chat.android.state.plugin.config.StatePluginConfig;
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel;

public class PM_LoginFragment extends Fragment {

    // All variables from your original LoginActivity
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvNoAccount;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public PM_LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout you copied from your original activity_login.xml
        return inflater.inflate(R.layout.fragment_pm_login, container, false);
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
                    Toast.makeText(getActivity(), "Login successful!", Toast.LENGTH_SHORT).show();
                    routeAfterAuth();
                })
                .addOnFailureListener(e -> showError(e))
                .addOnCompleteListener(task -> toggleLoading(false));
    }

    // This method is identical to the one in SignupFragment
    private void routeAfterAuth() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    boolean pmCompleted = doc != null && Boolean.TRUE.equals(doc.getBoolean("pmCompleted"));
                    if (pmCompleted) {
                        goToDashboard();
                    } else {
                        goToPMSetup();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to load profile. Directing to setup.", Toast.LENGTH_SHORT).show();
                    goToPMSetup();
                });
    }

    private void goToPMSetup() {
        if (getActivity() == null) return;
        Intent i = new Intent(getActivity(), PMAccSetup.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        getActivity().finish();
    }

    private void goToDashboard() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId= firebaseUser.getUid();
            String userName = firebaseUser.getDisplayName();

            String devTokenAPI = "thrwrmxh3e74";



            if (!ChatClient.isInitialized()|| ChatClient.instance().getCurrentUser() == null) {
                // LAST RESORT: Direct Constructor (Only works if the SDK allows a default config)
                StreamStatePluginFactory statePluginFactory = new StreamStatePluginFactory(
                        new StatePluginConfig(),getContext());

                ChatClient.Builder builder = new ChatClient.Builder(devTokenAPI, getContext())
                        .withPlugins(statePluginFactory) // <-- ADD THIS LINE
                        .logLevel(ChatLogLevel.ALL);
                ChatClient client = builder.build();
                // This initializes the static instance of ChatClient.
            }



            User user = new User.Builder()
                    .withId(userId)
                    .withName(userName)
                    .build();

            String userToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiNk5Za3RaNGpOcE5ObklETWJvTk9TbDFzQkZoMSJ9.xJFBZM52einVIxrllO_nKpR1cQso7MUsLNbAqbZLAT8";

            ChatClient.instance().connectUser(user, userToken).enqueue(result -> {
                if (result.isSuccess()) {
                    // User is connected to Stream, now proceed with navigation
                    Log.d("StreamConnect", "Successfully connected user to Stream.");

                    // Now navigate to the dashboard
                    Intent intent = new Intent(getActivity(), PMDashboardContainer.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    // Handle connection error
                    Log.e("StreamConnect", "Failed to connect user: " + result.errorOrNull().getMessage());
                    Toast.makeText(getActivity(), "Chat connection failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }

//        if (getActivity() == null) return;
//        Intent i = new Intent(getActivity(), PMDashboardContainer.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(i);
//        getActivity().finish();
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
