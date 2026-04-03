package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_info);

        ImageView btnBack = findViewById(R.id.btn_back);
        EditText etUsername = findViewById(R.id.et_username);
        EditText etEmail = findViewById(R.id.et_email);
        TextView tvError = findViewById(R.id.tv_error);
        Button btnContinue = findViewById(R.id.btn_continue);

        btnBack.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String email = etEmail.getText().toString();
            
            // Mock Duplicate Check
            if (username.equals("duplicate") || email.equals("duplicate@test.com")) {
                tvError.setVisibility(View.VISIBLE);
            } else {
                tvError.setVisibility(View.GONE);
                Intent intent = new Intent(RegisterInfoActivity.this, RegisterPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}
