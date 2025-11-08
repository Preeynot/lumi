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
        return position == 1 ? new PM_HostFragment() : new Renter_HostFragment();
    }

    @Override
    public int getItemCount() { return 2; }
}
