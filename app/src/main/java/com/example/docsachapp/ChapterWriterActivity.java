package com.example.docsachapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChapterWriterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_writer);

        ImageView btnBack = findViewById(R.id.btn_back);
        TextView btnPublish = findViewById(R.id.btn_publish);
        TextView btnDraft = findViewById(R.id.btn_draft);
        EditText etTitle = findViewById(R.id.et_chapter_title);
        EditText etContent = findViewById(R.id.et_chapter_content);

        btnBack.setOnClickListener(v -> finish());
        
        btnPublish.setOnClickListener(v -> {
            if (etTitle.getText().length() == 0 || etContent.getText().length() == 0) {
                Toast.makeText(this, "Vui lòng nhập tên chương và nội dung", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Xuất bản thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        
        btnDraft.setOnClickListener(v -> {
            Toast.makeText(this, "Đã lưu bản nháp", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
