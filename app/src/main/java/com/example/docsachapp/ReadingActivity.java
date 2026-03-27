package com.example.docsachapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ReadingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        ImageView btnBack = findViewById(R.id.btn_back);
        Spinner spinner = findViewById(R.id.spinner_chapters);
        Button btnFirst = findViewById(R.id.btn_first);
        Button btnPrev = findViewById(R.id.btn_prev);
        Button btnNext = findViewById(R.id.btn_next);
        Button btnLast = findViewById(R.id.btn_last);

        btnBack.setOnClickListener(v -> finish());

        String[] chapters = {"Chương 1", "Chương 2", "Chương 3", "Chương 4"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, chapters);
        spinner.setAdapter(adapter);

        // Mock pagination logic
        btnFirst.setEnabled(false);
        btnPrev.setEnabled(false);

        btnNext.setOnClickListener(v -> Toast.makeText(this, "Trang kế tiếp", Toast.LENGTH_SHORT).show());
        btnLast.setOnClickListener(v -> Toast.makeText(this, "Chương cuối cùng", Toast.LENGTH_SHORT).show());
    }
}
