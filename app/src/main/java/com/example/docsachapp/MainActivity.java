package com.example.docsachapp;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private View homeContent;
    private View mainContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các View cần thiết
        homeContent = findViewById(R.id.home_content_scroll); // Danh sách truyện trong activity_main
        mainContainer = findViewById(R.id.main_container);   // FrameLayout để chứa các Fragment
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Thiết lập sự kiện khi chọn item trên thanh điều hướng
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Nhấn nút LỊCH -> Quay về nội dung chính của activity_main (Danh sách truyện)
                homeContent.setVisibility(View.VISIBLE);
                mainContainer.setVisibility(View.GONE);
                return true;
            }

            // Đối với các nút khác, ẩn nội dung truyện và hiện main_container để nạp Fragment
            Fragment selectedFragment = null;
            
            if (itemId == R.id.nav_search) {
                // Nhấn nút TÌM KIẾM -> fragment_search
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.nav_list) {
                // Nhấn nút 3 GẠCH -> fragment_library
                selectedFragment = new LibraryFragment();
            } else if (itemId == R.id.nav_write) {
                // Nhấn nút CÂY BÚT -> fragment_tu_sach
                selectedFragment = new TuSachFragment();
            } else if (itemId == R.id.nav_profile) {
                // Nhấn nút TRÒN -> fragment_admin
                selectedFragment = new AdminFragment();
            }

            if (selectedFragment != null) {
                homeContent.setVisibility(View.GONE);
                mainContainer.setVisibility(View.VISIBLE);
                
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Mặc định khi mở App sẽ hiển thị màn hình chính (nút Lịch được chọn)
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}
