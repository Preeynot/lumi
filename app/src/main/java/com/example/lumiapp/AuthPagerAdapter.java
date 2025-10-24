package com.example.lumiapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AuthPagerAdapter extends FragmentStateAdapter {

    public AuthPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            // Position 1 (PM Tab) gets the PM Host
            return new PM_HostFragment();
        }

        // Position 0 (Renter Tab) gets the Renter Host
        return new Renter_HostFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
