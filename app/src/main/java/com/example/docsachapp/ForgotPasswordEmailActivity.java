package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordEmailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_email);

        ImageView btnBack = findViewById(R.id.btn_back);
        EditText etEmail = findViewById(R.id.et_email);
        TextView tvError = findViewById(R.id.tv_error);
        Button btnSend = findViewById(R.id.btn_send);

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            if (email.equals("error@test.com")) {
                tvError.setVisibility(View.VISIBLE);
            } else {
                tvError.setVisibility(View.GONE);
                Intent intent = new Intent(ForgotPasswordEmailActivity.this, ForgotPasswordOtpActivity.class);
                startActivity(intent);
            }
        });
    }
}
