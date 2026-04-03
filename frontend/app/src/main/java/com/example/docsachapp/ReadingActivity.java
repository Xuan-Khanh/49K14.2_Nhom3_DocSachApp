package com.example.docsachapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ReadingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        ImageView btnBack = findViewById(R.id.btn_back);
        TextView btnFirst = findViewById(R.id.btn_first);
        TextView btnPrev = findViewById(R.id.btn_prev);
        TextView btnNext = findViewById(R.id.btn_next);
        TextView btnLast = findViewById(R.id.btn_last);
        LinearLayout btnSelectChapter = findViewById(R.id.btn_select_chapter);

        btnBack.setOnClickListener(v -> finish());

        // Mock pagination logic
        btnFirst.setEnabled(false);
        btnPrev.setEnabled(false);

        btnNext.setOnClickListener(v -> Toast.makeText(this, "Trang kế tiếp", Toast.LENGTH_SHORT).show());
        btnLast.setOnClickListener(v -> Toast.makeText(this, "Chương cuối cùng", Toast.LENGTH_SHORT).show());
        
        btnSelectChapter.setOnClickListener(v -> Toast.makeText(this, "Chọn chương", Toast.LENGTH_SHORT).show());
    }
}
