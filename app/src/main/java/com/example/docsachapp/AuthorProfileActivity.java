package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AuthorProfileActivity extends AppCompatActivity {
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_profile);

        ImageView btnBack = findViewById(R.id.btn_back);
        Button btnFollowAuthor = findViewById(R.id.btn_follow_author);
        TextView itemBook = findViewById(R.id.item_book);

        btnBack.setOnClickListener(v -> finish());
        
        btnFollowAuthor.setOnClickListener(v -> {
            isFollowing = !isFollowing;
            if (isFollowing) {
                btnFollowAuthor.setText("Đang theo dõi");
                btnFollowAuthor.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary_transparent_25)));
                btnFollowAuthor.setTextColor(getResources().getColor(R.color.primary));
            } else {
                btnFollowAuthor.setText("Theo dõi tác giả");
                btnFollowAuthor.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
                btnFollowAuthor.setTextColor(getResources().getColor(R.color.white));
            }
        });
        
        itemBook.setOnClickListener(v -> {
            Intent intent = new Intent(AuthorProfileActivity.this, BookDetailsActivity.class);
            startActivity(intent);
        });
    }
}
