package com.example.docsachapp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

public class AuthorProfileActivity extends AppCompatActivity {
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_profile);

        ImageView btnBack = findViewById(R.id.btn_back);
        MaterialButton btnFollowAuthor = findViewById(R.id.btn_follow_author);

        btnBack.setOnClickListener(v -> finish());
        
        btnFollowAuthor.setOnClickListener(v -> {
            isFollowing = !isFollowing;
            if (isFollowing) {
                btnFollowAuthor.setText(R.string.following);
                int primaryTransparent = ContextCompat.getColor(this, R.color.primary_transparent_25);
                int primary = ContextCompat.getColor(this, R.color.primary);
                btnFollowAuthor.setBackgroundTintList(ColorStateList.valueOf(primaryTransparent));
                btnFollowAuthor.setTextColor(primary);
            } else {
                btnFollowAuthor.setText(R.string.follow_author);
                int primary = ContextCompat.getColor(this, R.color.primary);
                int white = ContextCompat.getColor(this, R.color.white);
                btnFollowAuthor.setBackgroundTintList(ColorStateList.valueOf(primary));
                btnFollowAuthor.setTextColor(white);
            }
        });
    }
}
