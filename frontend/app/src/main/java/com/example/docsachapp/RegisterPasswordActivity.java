package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.RegisterRequest;
import com.example.docsachapp.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * RegisterPasswordActivity.java
 * ==============================
 * Bước 2 đăng ký: nhập mật khẩu.
 * Nhận username + email từ RegisterInfoActivity qua Intent.
 * Sau khi nhập mật khẩu → gọi API POST /api/auth/register
 *
 * FIX: Tích hợp API đăng ký thật thay vì mock.
 */
public class RegisterPasswordActivity extends AppCompatActivity {
    private boolean isPassVis    = false;
    private boolean isConfirmVis = false;

    // Dữ liệu từ bước 1
    private String username;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_password);

        // ✅ FIX: Đọc dữ liệu được truyền từ RegisterInfoActivity
        username = getIntent().getStringExtra("username");
        email    = getIntent().getStringExtra("email");

        // Nếu không có dữ liệu → quay lại (tránh NullPointerException)
        if (username == null || email == null) {
            Toast.makeText(this, "Lỗi: thiếu thông tin đăng ký", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView  btnBack          = findViewById(R.id.btn_back);
        EditText   etPass           = findViewById(R.id.et_password);
        EditText   etConfirm        = findViewById(R.id.et_confirm_password);
        ImageView  ivEyePass        = findViewById(R.id.iv_eye_pass);
        ImageView  ivEyeConfirm     = findViewById(R.id.iv_eye_confirm);
        TextView   tvError          = findViewById(R.id.tv_error);
        Button     btnRegister      = findViewById(R.id.btn_register);
        ProgressBar progressBar     = findViewById(R.id.progress_bar);

        LinearLayout layoutRegisterForm = findViewById(R.id.layout_register_form);
        LinearLayout layoutSuccessPopup = findViewById(R.id.layout_success_popup);
        ImageView btnBackSuccess        = findViewById(R.id.btn_back_success);
        Button    btnBackToLogin        = findViewById(R.id.btn_back_to_login);

        btnBack.setOnClickListener(v -> finish());

        // Toggle hiện/ẩn mật khẩu
        ivEyePass.setOnClickListener(v -> {
            if (isPassVis) {
                etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            etPass.setSelection(etPass.getText().length());
            isPassVis = !isPassVis;
        });

        ivEyeConfirm.setOnClickListener(v -> {
            if (isConfirmVis) {
                etConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                etConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            etConfirm.setSelection(etConfirm.getText().length());
            isConfirmVis = !isConfirmVis;
        });

        // ✅ FIX: Nút Đăng ký gọi API thật
        btnRegister.setOnClickListener(v -> {
            String pass    = etPass.getText().toString();
            String confirm = etConfirm.getText().toString();

            // Validate mật khẩu
            if (pass.length() < 8) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText("Mật khẩu phải có ít nhất 8 ký tự");
                return;
            }
            if (!pass.equals(confirm)) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText("Mật khẩu xác nhận không khớp");
                return;
            }
            tvError.setVisibility(View.GONE);

            // Gọi API đăng ký
            callRegisterApi(pass, btnRegister, progressBar, layoutRegisterForm, layoutSuccessPopup);
        });

        btnBackSuccess.setOnClickListener(v -> {
            layoutSuccessPopup.setVisibility(View.GONE);
            layoutRegisterForm.setVisibility(View.VISIBLE);
        });

        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterPasswordActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    /**
     * Gọi API đăng ký: POST /api/auth/register
     */
    private void callRegisterApi(String password, Button btnRegister, ProgressBar progressBar,
                                 LinearLayout layoutForm, LinearLayout layoutSuccess) {
        // Hiện loading
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang đăng ký...");

        RegisterRequest request = new RegisterRequest(username, email, password);

        RetrofitClient.getApi().register(request).enqueue(new Callback<RegisterResponse>() {

            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse body = response.body();

                    if (body.getError() != null && !body.getError().isEmpty()) {
                        // Server báo lỗi trong body (ví dụ: username đã tồn tại)
                        Toast.makeText(RegisterPasswordActivity.this,
                            body.getError(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✅ Đăng ký thành công → tự động đăng nhập luôn (lưu token)
                    if (body.getToken() != null) {
                        SessionManager sessionManager = new SessionManager(RegisterPasswordActivity.this);
                        sessionManager.saveLoginSession(
                            body.getToken(),
                            body.getUserId(),
                            body.getUsername()
                        );
                    }

                    // Hiện popup thành công
                    layoutForm.setVisibility(View.GONE);
                    layoutSuccess.setVisibility(View.VISIBLE);

                } else {
                    // HTTP error (400 username trùng, 500 server, etc.)
                    String msg = "Đăng ký thất bại (mã " + response.code() + ")";
                    if (response.code() == 400) {
                        msg = "Username hoặc Email đã tồn tại";
                    }
                    Toast.makeText(RegisterPasswordActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");
                Toast.makeText(RegisterPasswordActivity.this,
                    "Không kết nối được server. Kiểm tra:\n" +
                    "1. Django đang chạy (python manage.py runserver)\n" +
                    "2. Dùng Emulator (không phải máy thật)",
                    Toast.LENGTH_LONG).show();
            }
        });
    }
}
