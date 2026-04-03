package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterMethodActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_method);

        Button btnGoogle = findViewById(R.id.btn_google);
        Button btnFacebook = findViewById(R.id.btn_facebook);
        Button btnEmail = findViewById(R.id.btn_email);
        TextView tvLoginLink = findViewById(R.id.tv_login_link);

        // Google / FB mock -> Main Activity directly
        btnGoogle.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterMethodActivity.this, MainActivity.class);
            startActivity(intent);
            finishAffinity();
        });
        
        btnFacebook.setOnClickListener(v -> btnGoogle.callOnClick());

        btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterMethodActivity.this, RegisterInfoActivity.class);
            startActivity(intent);
        });

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterMethodActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
