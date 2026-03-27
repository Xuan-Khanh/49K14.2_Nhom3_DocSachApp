package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterPasswordActivity extends AppCompatActivity {
    private boolean isPassVis = false;
    private boolean isConfirmVis = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_password);

        ImageView btnBack = findViewById(R.id.btn_back);
        EditText etPass = findViewById(R.id.et_password);
        EditText etConfirm = findViewById(R.id.et_confirm_password);
        ImageView ivEyePass = findViewById(R.id.iv_eye_pass);
        ImageView ivEyeConfirm = findViewById(R.id.iv_eye_confirm);
        TextView tvError = findViewById(R.id.tv_error);
        Button btnRegister = findViewById(R.id.btn_register);

        btnBack.setOnClickListener(v -> finish());

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

        btnRegister.setOnClickListener(v -> {
            String pass = etPass.getText().toString();
            if (pass.length() < 8) {
                tvError.setVisibility(View.VISIBLE);
            } else {
                tvError.setVisibility(View.GONE);
                Intent intent = new Intent(RegisterPasswordActivity.this, RegisterSuccessActivity.class);
                startActivity(intent);
            }
        });
    }
}
