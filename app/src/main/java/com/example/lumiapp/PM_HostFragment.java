package com.example.lumiapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PM_HostFragment extends Fragment implements AuthFragmentSwitcher {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pm_host, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            switchToSignup(); // Start with Signup screen
        }
    }

    @Override
    public void switchToLogin() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.pm_child_fragment_container, new PM_LoginFragment())
                .commit();
    }

    @Override
    public void switchToSignup() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.pm_child_fragment_container, new PM_SignupFragment())
                .commit();
    }
}
    