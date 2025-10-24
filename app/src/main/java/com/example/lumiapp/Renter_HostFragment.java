package com.example.lumiapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// This host fragment must implement the switcher interface to manage its children.
public class Renter_HostFragment extends Fragment implements AuthFragmentSwitcher {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout that only contains the child fragment container.
        return inflater.inflate(R.layout.fragment_renter_host, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ THIS IS THE FIX ✅
        // When this Renter tab is first shown, we tell it to display
        // the Renter_SignupFragment inside its container by default.
        if (savedInstanceState == null) {
            switchToSignup();
        }
    }

    @Override
    public void switchToLogin() {
        // Use getChildFragmentManager() because this is a fragment managing other fragments.
        if (isAdded()) { // Safety check to ensure fragment is attached to an activity
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.renter_child_fragment_container, new Renter_LoginFragment())
                    .commit();
        }
    }

    @Override
    public void switchToSignup() {
        if (isAdded()) { // Safety check
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.renter_child_fragment_container, new Renter_SignupFragment())
                    .commit();
        }
    }
}
