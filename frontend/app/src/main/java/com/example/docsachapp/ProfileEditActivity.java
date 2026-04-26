package com.example.docsachapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.UserProfile;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ProfileEditActivity – Chỉnh sửa hồ sơ người dùng
 */
public class ProfileEditActivity extends AppCompatActivity {

    private EditText etUsername, etBio, etBirthday, etEmail;
    private Button btnSave;
    private ImageView btnBack, ivAvatar;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

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
        etUsername  = findViewById(R.id.et_username);
        etBio       = findViewById(R.id.et_bio);
        etBirthday  = findViewById(R.id.et_birthday);
        etEmail     = findViewById(R.id.et_email);
        ivAvatar    = findViewById(R.id.iv_avatar);
        progressBar = findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String username = etUsername != null ? etUsername.getText().toString().trim() : "";
            String bio      = etBio      != null ? etBio.getText().toString().trim() : "";

            if (username.isEmpty()) {
                if (etUsername != null) etUsername.setError("Không được để trống");
                return;
            }
            saveProfile(username, bio);
        });
    }

    private void loadProfile(String token) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getApi().getUserProfile(token).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(@NonNull Call<UserProfile> call, @NonNull Response<UserProfile> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    fillForm(currentProfile);
                } else {
                    Toast.makeText(ProfileEditActivity.this, "Không thể tải hồ sơ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileEditActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillForm(UserProfile profile) {
        if (etUsername != null) etUsername.setText(profile.getUsername());
        if (etBio != null)      etBio.setText(profile.getBio());
        if (etBirthday != null) etBirthday.setText(profile.getBirthday() != null ? profile.getBirthday() : "");
        if (etEmail != null)    etEmail.setText(profile.getEmail());
        
        if (ivAvatar != null && profile.getAvatar() != null) {
            Glide.with(this).load(profile.getAvatar()).placeholder(android.R.drawable.ic_menu_gallery).into(ivAvatar);
        }
    }

    private void saveProfile(String username, String bio) {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Tạo Map RequestBody cho các trường văn bản (Multipart PartMap)
        Map<String, RequestBody> parts = new HashMap<>();
        parts.put("username", RequestBody.create(username, MediaType.parse("text/plain")));
        parts.put("mo_ta", RequestBody.create(bio, MediaType.parse("text/plain")));
        
        // Hiện tại để avatar null (Chưa xử lý chọn file ảnh)
        MultipartBody.Part avatarPart = null;

        // Gọi API với 3 tham số: token, PartMap, và avatarPart
        RetrofitClient.getApi().updateUserProfile(token, parts, avatarPart).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(ProfileEditActivity.this, "Đã lưu hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ProfileEditActivity.this, "Lưu thất bại (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(ProfileEditActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
