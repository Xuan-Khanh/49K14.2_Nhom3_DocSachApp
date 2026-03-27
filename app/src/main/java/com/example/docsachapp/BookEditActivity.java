package com.example.docsachapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BookEditActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_edit);

        Button btnAddChapter = findViewById(R.id.btn_add_chapter);
        Button btnDeleteBook = findViewById(R.id.btn_delete_book);
        android.widget.ImageView btnChapterSettings = findViewById(R.id.btn_chapter_settings);

        btnChapterSettings.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, btnChapterSettings);
            popup.getMenu().add("Ẩn/Hiện chương (Publish/Unpublish)");
            popup.getMenu().add("Xóa chương");
            popup.setOnMenuItemClickListener(item -> {
                Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            });
            popup.show();
        });

        btnAddChapter.setOnClickListener(v -> {
            Intent intent = new Intent(BookEditActivity.this, ChapterWriterActivity.class);
            startActivity(intent);
        });

        btnDeleteBook.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Xóa truyện này vĩnh viễn?")
                .setMessage("Tác phẩm này sẽ không thể khôi phục sau khi xóa.")
                .setPositiveButton("Xóa", (d, w) -> {
                    Toast.makeText(this, "Đã xóa truyện", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }
}
