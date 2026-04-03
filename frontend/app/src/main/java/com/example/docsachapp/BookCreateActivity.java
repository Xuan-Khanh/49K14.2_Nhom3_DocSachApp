package com.example.docsachapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BookCreateActivity extends AppCompatActivity {
    private boolean isFormValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_create);

        ImageView btnClose = findViewById(R.id.btn_close);
        ImageView btnNext = findViewById(R.id.btn_next);
        EditText etTitle = findViewById(R.id.et_book_title);
        EditText etDesc = findViewById(R.id.et_book_description);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (!etTitle.getText().toString().trim().isEmpty() && !etDesc.getText().toString().trim().isEmpty()) {
                    isFormValid = true;
                    btnNext.setColorFilter(ContextCompat.getColor(BookCreateActivity.this, R.color.text_dark));
                } else {
                    isFormValid = false;
                    btnNext.setColorFilter(ContextCompat.getColor(BookCreateActivity.this, R.color.placeholder));
                }
            }
        };

        etTitle.addTextChangedListener(watcher);
        etDesc.addTextChangedListener(watcher);

        btnClose.setOnClickListener(v -> {
            if (etTitle.getText().length() > 0 || etDesc.getText().length() > 0) {
                new AlertDialog.Builder(this)
                    .setTitle("Hủy tt Thêm")
                    .setMessage("Bạn có muốn hủy thông tin này và lưu lại dưới dạng bản thảo?")
                    .setPositiveButton("Đồng ý", (d, w) -> {
                        Toast.makeText(this, "Đã lưu bản thảo", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            } else {
                finish();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (isFormValid) {
                Intent intent = new Intent(BookCreateActivity.this, BookCreateStep2Activity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
