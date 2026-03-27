package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.fragment.app.Fragment;
import com.example.docsachapp.HomeFragment;
import com.example.docsachapp.SearchFragment;
import com.example.docsachapp.FollowedFragment;
import com.example.docsachapp.LibraryFragment;
import com.example.docsachapp.AdminFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mock authentication check - start LoginActivity first for now
        // Intent intent = new Intent(this, LoginActivity.class);
        // startActivity(intent);
        // finish();
        
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.nav_followed) {
                selectedFragment = new LibraryFragment(); // 3rd icon is Thư viện
            } else if (itemId == R.id.nav_library) {
                selectedFragment = new TuSachFragment(); // 4th icon is Tủ sách
            } else if (itemId == R.id.nav_admin) {
                selectedFragment = new AdminFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container, selectedFragment).commit();
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}
