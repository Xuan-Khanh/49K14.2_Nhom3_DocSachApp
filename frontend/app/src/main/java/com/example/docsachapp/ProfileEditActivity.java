package com.example.docsachapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.UserProfile;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ProfileEditActivity – Chỉnh sửa hồ sơ người dùng
 *
 * Luồng:
 * 1. Load profile hiện tại từ GET /api/auth/profile
 * 2. Hiển thị dữ liệu vào EditText
 * 3. Khi bấm Lưu → gọi PUT /api/auth/profile với các trường đã thay đổi
 * 4. Sau khi lưu thành công → finish() để quay lại AdminFragment
 */
public class ProfileEditActivity extends AppCompatActivity {

    private EditText etBio, etBirthday, etEmail;
    private Button btnSave;
    private ImageView btnBack;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    // Giữ giá trị gốc để không gửi trùng
    private UserProfile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        sessionManager = new SessionManager(this);
        String token = sessionManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadProfile(token);
    }

    private void initViews() {
        btnBack     = findViewById(R.id.btn_back);
        btnSave     = findViewById(R.id.btn_save);
        etBio       = findViewById(R.id.et_bio);          // Mô tả / giới thiệu bản thân
        etBirthday  = findViewById(R.id.et_birthday);     // Ngày sinh (yyyy-MM-dd)
        etEmail     = findViewById(R.id.et_email);        // Email
        progressBar = findViewById(R.id.progress_bar);

        // Fallback: nếu layout dùng et_username thay cho et_bio
        if (etBio == null) etBio = findViewById(R.id.et_username);

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String bio      = etBio      != null ? etBio.getText().toString().trim() : "";
            String birthday = etBirthday != null ? etBirthday.getText().toString().trim() : "";
            String email    = etEmail    != null ? etEmail.getText().toString().trim() : "";

            if (etBio != null && bio.isEmpty()) {
                etBio.setError("Không được để trống");
                return;
            }
            saveProfile(bio, birthday, email);
        });
    }

    // ─── Load profile hiện tại ─────────────────────────────────────────────────
    private void loadProfile(String token) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getApi().getUserProfile(token).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    fillForm(currentProfile);
                } else {
                    Toast.makeText(ProfileEditActivity.this,
                            "Không thể tải hồ sơ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileEditActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillForm(UserProfile profile) {
        if (etBio != null)      etBio.setText(profile.getBio());
        if (etBirthday != null) etBirthday.setText(profile.getBirthday() != null ? profile.getBirthday() : "");
        if (etEmail != null)    etEmail.setText(profile.getEmail());
    }

    // ─── Lưu profile ─────────────────────────────────────────────────────────
    private void saveProfile(String bio, String birthday, String email) {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        Map<String, String> body = new HashMap<>();
        body.put("mo_ta", bio);
        if (!birthday.isEmpty()) body.put("ngay_sinh", birthday);
        if (!email.isEmpty())    body.put("email", email);

        RetrofitClient.getApi().updateUserProfile(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(ProfileEditActivity.this,
                            "Đã lưu hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    // Cache avatar nếu profile trả về
                    if (currentProfile != null && currentProfile.getAvatar() != null) {
                        sessionManager.saveAvatar(currentProfile.getAvatar());
                    }
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = response.code() == 400
                            ? "Dữ liệu không hợp lệ"
                            : "Lưu thất bại (mã " + response.code() + ")";
                    Toast.makeText(ProfileEditActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(ProfileEditActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
