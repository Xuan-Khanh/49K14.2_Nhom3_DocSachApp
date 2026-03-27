package com.example.docsachapp;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class CollectionDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_details);

        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView btnMore = findViewById(R.id.btn_more);

        btnBack.setOnClickListener(v -> finish());
        
        btnMore.setOnClickListener(v -> {
            // Mock menu
             android.widget.PopupMenu popup = new android.widget.PopupMenu(this, btnMore);
             popup.getMenu().add("Thêm truyện");
             popup.getMenu().add("Chỉnh sửa Bộ Sưu Tập");
             popup.getMenu().add("Xóa bộ sưu tập");
             popup.show();
        });
    }
}
