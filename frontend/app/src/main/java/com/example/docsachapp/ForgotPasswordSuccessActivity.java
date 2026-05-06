package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

// Màn hình Bước 4 (Cuối cùng): Thông báo đổi mật khẩu thành công và Hướng dẫn về trang Đăng nhập
public class ForgotPasswordSuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_success);

        ImageView btnBack = findViewById(R.id.btn_back);
        Button btnBackToLogin = findViewById(R.id.btn_back_to_login);

        btnBack.setOnClickListener(v -> finish());

        // Bấm nút "Quay lại Đăng nhập"
        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordSuccessActivity.this, LoginActivity.class);
            // Xóa sạch lịch sử màn hình, biến LoginActivity thành màn hình gốc mới
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
