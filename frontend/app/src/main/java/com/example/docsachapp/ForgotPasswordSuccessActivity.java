package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordSuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_success);

        ImageView btnBack = findViewById(R.id.btn_back);
        Button btnBackToLogin = findViewById(R.id.btn_back_to_login);

        btnBack.setOnClickListener(v -> finish());

        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordSuccessActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
