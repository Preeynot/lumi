package com.example.lumiapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        // The adapter connects our host fragments to the ViewPager
        AuthPagerAdapter adapter = new AuthPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // This links the tabs to the ViewPager, setting the text for each tab
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 1) {
                tab.setText("Property Manager");
            } else {
                tab.setText("Renter");
            }
        }).attach();
    }
}
