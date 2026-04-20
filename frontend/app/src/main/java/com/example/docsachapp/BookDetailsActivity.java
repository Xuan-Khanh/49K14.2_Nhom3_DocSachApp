package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.model.Story;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.makeramen.roundedimageview.RoundedImageView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailsActivity extends AppCompatActivity {
    private boolean isFollowing = false;
    private int storyId;
    private int authorId = -1;
    
    private TextView tvTitle, tvAuthor, tvDescription, tvRating;
    private RoundedImageView ivCover, ivAuthorAvatar;
    private ChipGroup cgGenres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        storyId = getIntent().getIntExtra("STORY_ID", -1);
        if (storyId == -1) {
            Toast.makeText(this, "Không tìm thấy truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadStoryDetails();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvAuthor = findViewById(R.id.tv_author);
        tvDescription = findViewById(R.id.tv_description);
        tvRating = findViewById(R.id.tv_rating_score);
        ivCover = findViewById(R.id.iv_book_cover);
        ivAuthorAvatar = findViewById(R.id.iv_author_avatar);
        cgGenres = findViewById(R.id.cg_genres); // Ánh xạ ChipGroup thể loại
        
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        
        // Sự kiện click vào tác giả
        View.OnClickListener authorClick = v -> {
            if (authorId != -1) {
                Intent intent = new Intent(BookDetailsActivity.this, AuthorProfileActivity.class);
                intent.putExtra("AUTHOR_ID", authorId);
                startActivity(intent);
            }
        };
        
        tvAuthor.setOnClickListener(authorClick);
        if (ivAuthorAvatar != null) ivAuthorAvatar.setOnClickListener(authorClick);

        findViewById(R.id.btn_read).setOnClickListener(v -> {
            Intent intent = new Intent(this, ReadingActivity.class);
            intent.putExtra("STORY_ID", storyId);
            startActivity(intent);
        });

        findViewById(R.id.btn_comments).setOnClickListener(v -> {
            Intent intent = new Intent(this, CommentsActivity.class);
            intent.putExtra("STORY_ID", storyId);
            startActivity(intent);
        });
    }

    private void loadStoryDetails() {
        RetrofitClient.getApi().getStoryDetail(storyId).enqueue(new Callback<Story>() {
            @Override
            public void onResponse(Call<Story> call, Response<Story> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Story story = response.body();
                    if (story.getAuthor() != null) authorId = story.getAuthor().getId();
                    displayStory(story);
                }
            }
            @Override
            public void onFailure(Call<Story> call, Throwable t) {}
        });
    }

    private void displayStory(Story story) {
        tvTitle.setText(story.getTitle());
        tvAuthor.setText(story.getAuthorName());
        tvDescription.setText(story.getDescription());
        tvRating.setText(String.valueOf(story.getRating()));
        
        Glide.with(this).load(story.getCoverUrl()).placeholder(R.drawable.biatruyen).into(ivCover);
        if (ivAuthorAvatar != null && story.getAuthor() != null && story.getAuthor().getAvatar() != null) {
            Glide.with(this).load(story.getAuthor().getAvatar())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .circleCrop().into(ivAuthorAvatar);
        }

        // Đổ dữ liệu Thể loại vào ChipGroup
        if (cgGenres != null && story.getGenres() != null) {
            cgGenres.removeAllViews(); // Xóa các chip cũ
            for (Story.Genre genre : story.getGenres()) {
                Chip chip = new Chip(this);
                chip.setText(genre.getName());
                chip.setChipBackgroundColorResource(R.color.gray_bg); // Màu nền xám nhạt
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
                chip.setTextSize(12);
                chip.setClickable(true);
                // Bạn có thể thêm sự kiện click vào chip để tìm kiếm theo thể loại ở đây
                cgGenres.addView(chip);
            }
        }
    }
}