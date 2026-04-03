package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.LoginRequest;
import com.example.docsachapp.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LoginActivity.java
 * ===================
 * Màn hình đăng nhập - gọi API Django POST /api/auth/login
 *
 * Flow:
 * 1. User nhập username + password
 * 2. Bấm Đăng nhập → gọi API
 * 3. Nếu thành công → lưu token vào SessionManager → vào MainActivity
 * 4. Nếu thất bại   → hiện thông báo lỗi
 */
public class LoginActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ── Ánh xạ View ───────────────────────────────────────────────────────
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        ImageView ivEye          = findViewById(R.id.iv_eye);
        btnLogin                 = findViewById(R.id.btn_login);
        TextView tvRegisterLink  = findViewById(R.id.tv_register_link);
        TextView tvForgotPassword= findViewById(R.id.tv_forgot_password);
        progressBar              = findViewById(R.id.progress_bar);

        // ── Hiện/ẩn mật khẩu ─────────────────────────────────────────────────
        ivEye.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                etPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            etPassword.setSelection(etPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        // ── Nút Đăng nhập ────────────────────────────────────────────────────
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate cơ bản trước khi gọi API
            if (username.isEmpty()) {
                etUsername.setError("Vui lòng nhập tên đăng nhập");
                etUsername.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Vui lòng nhập mật khẩu");
                etPassword.requestFocus();
                return;
            }

            // Gọi API đăng nhập
            callLoginApi(username, password);
        });

        // ── Quên mật khẩu ────────────────────────────────────────────────────
        tvForgotPassword.setOnClickListener(v ->
            startActivity(new Intent(LoginActivity.this, ForgotPasswordEmailActivity.class))
        );

        // ── Chuyển sang Đăng ký ───────────────────────────────────────────────
        tvRegisterLink.setOnClickListener(v ->
            startActivity(new Intent(LoginActivity.this, RegisterMethodActivity.class))
        );
    }

    /**
     * Gọi API đăng nhập Django: POST /api/auth/login
     */
    private void callLoginApi(String username, String password) {
        // Hiện loading, ẩn nút để tránh bấm liên tục
        showLoading(true);

        LoginRequest request = new LoginRequest(username, password);

        RetrofitClient.getApi().login(request).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();

                    // ✅ Đăng nhập thành công → lưu token
                    SessionManager sessionManager = new SessionManager(LoginActivity.this);
                    sessionManager.saveLoginSession(
                        body.getToken(),
                        body.getUserId(),
                        body.getUsername()
                    );

                    Toast.makeText(LoginActivity.this,
                        "Chào mừng " + body.getUsername() + "!", Toast.LENGTH_SHORT).show();

                    // Chuyển sang MainActivity và xóa stack cũ
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    // ❌ Server trả về lỗi (sai mật khẩu, etc.)
                    if (response.code() == 401) {
                        Toast.makeText(LoginActivity.this,
                            "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                            "Lỗi server: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // ❌ Không kết nối được (mạng, server chưa chạy, URL sai)
                showLoading(false);
                Toast.makeText(LoginActivity.this,
                    "Không kết nối được server. Kiểm tra:\n" +
                    "1. Django đang chạy (python manage.py runserver)\n" +
                    "2. Dùng Emulator (không phải máy thật)",
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Hiện/ẩn loading spinner và enable/disable nút login
     */
    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnLogin.setEnabled(!isLoading);
        btnLogin.setText(isLoading ? "Đang đăng nhập..." : "Đăng nhập");
    }
}
