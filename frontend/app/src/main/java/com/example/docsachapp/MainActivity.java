package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.docsachapp.api.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private LinearLayout llTopBar;
    private View vDivider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        llTopBar = findViewById(R.id.ll_top_bar);
        vDivider = findViewById(R.id.v_divider);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.nav_list) {
                selectedFragment = new LibraryFragment();
            } else if (itemId == R.id.nav_write) {
                selectedFragment = new TuSachFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new AdminFragment();
            }

            if (selectedFragment != null) {
                // Ẩn/Hiện Top Bar dựa trên Fragment được chọn
                // Ẩn khi vào trang Thư viện (LibraryFragment) hoặc trang Viết (TuSachFragment)
                if (selectedFragment instanceof LibraryFragment || selectedFragment instanceof TuSachFragment) {
                    llTopBar.setVisibility(View.GONE);
                    vDivider.setVisibility(View.GONE);
                } else {
                    llTopBar.setVisibility(View.VISIBLE);
                    vDivider.setVisibility(View.VISIBLE);
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}
