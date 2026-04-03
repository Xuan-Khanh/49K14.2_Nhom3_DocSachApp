package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BookDetailsActivity extends AppCompatActivity {
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        ImageView btnBack = findViewById(R.id.btn_back);
        com.google.android.material.button.MaterialButton btnRead = findViewById(R.id.btn_read);
        com.google.android.material.button.MaterialButton btnFollowBook = findViewById(R.id.btn_follow_book);
        LinearLayout btnCollection = findViewById(R.id.btn_collection);
        com.google.android.material.button.MaterialButton btnComments = findViewById(R.id.btn_comments);
        TextView tvAuthor = findViewById(R.id.tv_author);
        TextView tvRatingScore = findViewById(R.id.tv_rating_score);
        
        LinearLayout llStars = findViewById(R.id.ll_stars);
        ImageView[] stars = new ImageView[5];
        for (int i = 0; i < 5; i++) {
            stars[i] = (ImageView) llStars.getChildAt(i);
        }

        btnBack.setOnClickListener(v -> finish());

        tvAuthor.setOnClickListener(v -> {
            // Later: transition to Author Profile
        });

        btnRead.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailsActivity.this, ReadingActivity.class);
            startActivity(intent);
        });

        btnFollowBook.setOnClickListener(v -> {
            isFollowing = !isFollowing;
            if (isFollowing) {
                btnFollowBook.setText("Bỏ theo dõi truyện");
                btnFollowBook.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_transparent_25)));
                btnFollowBook.setTextColor(ContextCompat.getColor(this, R.color.primary));
            } else {
                btnFollowBook.setText("Theo dõi truyện");
                btnFollowBook.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
                btnFollowBook.setTextColor(ContextCompat.getColor(this, R.color.white));
            }
        });

        btnCollection.setOnClickListener(v -> Toast.makeText(this, "Đã thêm vào bộ sưu tập", Toast.LENGTH_SHORT).show());

        btnComments.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailsActivity.this, CommentsActivity.class);
            startActivity(intent);
        });

        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnClickListener(v -> {
                tvRatingScore.setText("Đánh giá của bạn");
                for (int j = 0; j <= index; j++) {
                    stars[j].setColorFilter(ContextCompat.getColor(this, R.color.primary));
                }
                for (int j = index + 1; j < stars.length; j++) {
                    stars[j].setColorFilter(ContextCompat.getColor(this, R.color.text_dark));
                }
            });
        }
    }
}
