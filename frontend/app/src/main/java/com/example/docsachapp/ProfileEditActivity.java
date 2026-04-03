package com.example.docsachapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileEditActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        ImageView btnBack = findViewById(R.id.btn_back);
        Button btnSave = findViewById(R.id.btn_save);
        EditText etName = findViewById(R.id.et_username);

        btnBack.setOnClickListener(v -> finish());
        
        btnSave.setOnClickListener(v -> {
            if (etName.getText().toString().trim().isEmpty()) {
                etName.setError("Tên hiển thị không được để trống");
            } else {
                Toast.makeText(this, "Đã lưu thông tin", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
