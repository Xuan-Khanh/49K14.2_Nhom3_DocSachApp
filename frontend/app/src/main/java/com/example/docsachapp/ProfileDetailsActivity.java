package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.UserProfile;
import com.makeramen.roundedimageview.RoundedImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Màn hình xem chi tiết Hồ sơ (Profile) của người dùng hiện tại
public class ProfileDetailsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView tvUsername, tvEmail, tvBirthday, tvBio;
    private RoundedImageView ivAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);

        sessionManager = new SessionManager(this);

        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        tvBirthday = findViewById(R.id.tv_birthday);
        tvBio = findViewById(R.id.tv_bio);
        ivAvatar = findViewById(R.id.iv_avatar);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        findViewById(R.id.btn_edit).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileEditActivity.class));
        });

        // Xử lý nút Đăng xuất (hiển thị Dialog xác nhận trước khi thoát)
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        sessionManager.logout(); // Xóa Token khỏi SharedPreferences
                        // Trở về trang Đăng nhập và xóa hết các màn hình cũ
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        loadProfile();
    }

    // Lấy thông tin user từ API
    private void loadProfile() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;

        RetrofitClient.getApi().getUserProfile(token).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayProfile(response.body());
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
            }
        });
    }

    private void displayProfile(UserProfile profile) {
        if (tvUsername != null) tvUsername.setText(profile.getUsername());
        if (tvEmail != null) tvEmail.setText(profile.getEmail());
        if (tvBio != null) tvBio.setText(profile.getBio().isEmpty() ? "Chưa có mô tả" : profile.getBio());
        
        if (ivAvatar != null && profile.getAvatar() != null) {
            Glide.with(this).load(profile.getAvatar())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .circleCrop().into(ivAvatar);
        }
    }
}
