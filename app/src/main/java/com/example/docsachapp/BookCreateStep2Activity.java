package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BookCreateStep2Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_create_step2);

        ImageView btnClose = findViewById(R.id.btn_close);
        ImageView btnDone = findViewById(R.id.btn_done);
        TextView btnAddChapter = findViewById(R.id.btn_add_chapter);

        btnClose.setOnClickListener(v -> finish());
        
        btnDone.setOnClickListener(v -> {
            Toast.makeText(this, "Thêm truyện thành công", Toast.LENGTH_SHORT).show();
            // In a real app we'd clear top back to MainActivity/TuSachFragment
            finish();
        });
        
        btnAddChapter.setOnClickListener(v -> {
            Intent intent = new Intent(BookCreateStep2Activity.this, ChapterWriterActivity.class);
            startActivity(intent);
        });
    }
}
