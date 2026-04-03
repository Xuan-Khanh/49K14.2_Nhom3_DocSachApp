package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordOtpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_otp);

        ImageView btnBack = findViewById(R.id.btn_back);
        EditText etOtp = findViewById(R.id.et_otp);
        TextView tvError = findViewById(R.id.tv_error);
        Button btnVerify = findViewById(R.id.btn_verify);

        btnBack.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> {
            String otp = etOtp.getText().toString();
            if (otp.equals("0000")) {
                tvError.setVisibility(View.VISIBLE);
            } else {
                tvError.setVisibility(View.GONE);
                Intent intent = new Intent(ForgotPasswordOtpActivity.this, ForgotPasswordResetActivity.class);
                startActivity(intent);
            }
        });
    }
}
