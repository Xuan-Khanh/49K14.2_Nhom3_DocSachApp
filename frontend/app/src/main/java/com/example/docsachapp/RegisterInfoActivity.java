package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * RegisterInfoActivity.java
 * ==========================
 * Bước 1 đăng ký: nhập Username và Email.
 * Dữ liệu được truyền sang RegisterPasswordActivity qua Intent.
 */
public class RegisterInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_info);

        ImageView btnBack      = findViewById(R.id.btn_back);
        EditText  etUsername   = findViewById(R.id.et_username);
        EditText  etEmail      = findViewById(R.id.et_email);
        TextView  tvError      = findViewById(R.id.tv_error);
        Button    btnContinue  = findViewById(R.id.btn_continue);

        btnBack.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email    = etEmail.getText().toString().trim();

            // Validate
            if (username.isEmpty()) {
                etUsername.setError("Vui lòng nhập tên đăng nhập");
                etUsername.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                etEmail.setError("Vui lòng nhập email");
                etEmail.requestFocus();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Email không hợp lệ");
                etEmail.requestFocus();
                return;
            }

            tvError.setVisibility(View.GONE);

            // ✅ FIX: Truyền username và email sang bước tiếp theo
            Intent intent = new Intent(RegisterInfoActivity.this, RegisterPasswordActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("email", email);
            startActivity(intent);
        });
    }
}
