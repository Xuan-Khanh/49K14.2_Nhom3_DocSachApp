package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BookDetailsActivity extends AppCompatActivity {
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        ImageView btnBack = findViewById(R.id.btn_back);
        Button btnRead = findViewById(R.id.btn_read);
        Button btnFollowBook = findViewById(R.id.btn_follow_book);
        Button btnCollection = findViewById(R.id.btn_collection);
        Button btnComments = findViewById(R.id.btn_comments);
        TextView tvAuthor = findViewById(R.id.tv_author);
        TextView tvRatingScore = findViewById(R.id.tv_rating_score);
        
        ImageView[] stars = {
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5)
        };

        btnBack.setOnClickListener(v -> finish());

        tvAuthor.setOnClickListener(v -> {
            // Later: transition to Author Profile
        });

        btnRead.setOnClickListener(v -> {
            // Hover logic conceptually simulated, but click takes us to read
            Intent intent = new Intent(BookDetailsActivity.this, ReadingActivity.class);
            startActivity(intent);
        });

        btnFollowBook.setOnClickListener(v -> {
            isFollowing = !isFollowing;
            if (isFollowing) {
                btnFollowBook.setText("Bỏ theo dõi truyện");
                btnFollowBook.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary_transparent_25)));
                btnFollowBook.setTextColor(getResources().getColor(R.color.primary));
            } else {
                btnFollowBook.setText("Theo dõi truyện");
                btnFollowBook.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
                btnFollowBook.setTextColor(getResources().getColor(R.color.white));
            }
        });

        btnCollection.setOnClickListener(v -> {
            Toast.makeText(this, "Thêm thành công vào BST", Toast.LENGTH_SHORT).show();
        });

        btnComments.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailsActivity.this, CommentsActivity.class);
            startActivity(intent);
        });

        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnClickListener(v -> {
                tvRatingScore.setText("Your Rating");
                for (int j = 0; j <= index; j++) {
                    stars[j].setColorFilter(getResources().getColor(R.color.primary));
                }
                for (int j = index + 1; j < stars.length; j++) {
                    stars[j].setColorFilter(getResources().getColor(R.color.text_dark));
                }
            });
        }
    }
}
