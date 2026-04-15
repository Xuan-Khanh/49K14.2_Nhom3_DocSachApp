package com.example.docsachapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docsachapp.adapter.StorySearchAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.model.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorProfileActivity extends AppCompatActivity {
    private boolean isFollowing = false;
    private int userId;
    
    private RoundedImageView ivAvatar;
    private TextView tvStoryCount, tvFollowerCount, tvFollowingCount;
    private TextView tvUsername, tvBio, tvStoriesHeader;
    private MaterialButton btnFollowAuthor;
    private RecyclerView rvAuthorStories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_profile);

        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            finish();
            return;
        }

        initViews();
        loadUserProfile();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        ivAvatar = findViewById(R.id.iv_avatar);
        tvStoryCount = findViewById(R.id.tv_story_count);
        tvFollowerCount = findViewById(R.id.tv_follower_count);
        tvFollowingCount = findViewById(R.id.tv_following_count);
        tvUsername = findViewById(R.id.tv_username);
        tvBio = findViewById(R.id.tv_bio);
        tvStoriesHeader = findViewById(R.id.tv_stories_header);
        btnFollowAuthor = findViewById(R.id.btn_follow_author);
        rvAuthorStories = findViewById(R.id.rv_author_stories);

        rvAuthorStories.setLayoutManager(new LinearLayoutManager(this));

        btnFollowAuthor.setOnClickListener(v -> toggleFollow());

        android.widget.LinearLayout llFollowers = findViewById(R.id.ll_followers);
        android.widget.LinearLayout llFollowing = findViewById(R.id.ll_following);

        llFollowers.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(AuthorProfileActivity.this, FollowListActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", tvUsername.getText().toString());
            intent.putExtra("TAB_INITIAL", 0); // 0 for followers
            startActivity(intent);
        });

        llFollowing.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(AuthorProfileActivity.this, FollowListActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", tvUsername.getText().toString());
            intent.putExtra("TAB_INITIAL", 1); // 1 for following
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        RetrofitClient.getApi().getPublicProfile(userId).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();
                    
                    tvUsername.setText(profile.getUsername());
                    tvStoryCount.setText(String.valueOf(profile.getStoryCount()));
                    tvFollowerCount.setText(String.valueOf(profile.getFollowerCount()));
                    tvFollowingCount.setText(String.valueOf(profile.getFollowingCount()));
                    tvBio.setText(profile.getBio());

                    if (profile.getAvatar() != null && !profile.getAvatar().isEmpty()) {
                        String fullUrl = profile.getAvatar().startsWith("http") ? profile.getAvatar() 
                                         : "http://10.0.2.2:8000" + profile.getAvatar();
                        Glide.with(AuthorProfileActivity.this).load(fullUrl).placeholder(R.drawable.image5).into(ivAvatar);
                    } else {
                        ivAvatar.setImageResource(R.drawable.image5);
                    }

                    if (profile.getPublishedStories() != null) {
                        tvStoriesHeader.setText("Truyện đã đăng (" + profile.getPublishedStories().size() + ")");
                        StorySearchAdapter adapter = new StorySearchAdapter(profile.getPublishedStories(), AuthorProfileActivity.this);
                        rvAuthorStories.setAdapter(adapter);
                    }

                    isFollowing = profile.isFollowing();
                    updateFollowButtonUI();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Toast.makeText(AuthorProfileActivity.this, "Lỗi tải thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFollow() {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");
        if (token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Integer> body = new HashMap<>();
        body.put("following_id", userId);

        if (isFollowing) {
            RetrofitClient.getApi().unfollowUser("Token " + token, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        isFollowing = false;
                        updateFollowButtonUI();
                        // Trừ một follow (tối thiểu 0)
                        int currentCount = Integer.parseInt(tvFollowerCount.getText().toString());
                        tvFollowerCount.setText(String.valueOf(Math.max(0, currentCount - 1)));
                    } else {
                        Toast.makeText(AuthorProfileActivity.this, "Lỗi bỏ theo dõi", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) { }
            });
        } else {
            RetrofitClient.getApi().followUser("Token " + token, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        isFollowing = true;
                        updateFollowButtonUI();
                        int currentCount = Integer.parseInt(tvFollowerCount.getText().toString());
                        tvFollowerCount.setText(String.valueOf(currentCount + 1));
                    } else {
                        Toast.makeText(AuthorProfileActivity.this, "Lỗi theo dõi", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) { }
            });
        }
    }

    private void updateFollowButtonUI() {
        if (isFollowing) {
            btnFollowAuthor.setText("Đang theo dõi");
            int primaryTransparent = ContextCompat.getColor(this, R.color.primary_transparent_25);
            int primary = ContextCompat.getColor(this, R.color.primary);
            btnFollowAuthor.setBackgroundTintList(ColorStateList.valueOf(primaryTransparent));
            btnFollowAuthor.setTextColor(primary);
        } else {
            btnFollowAuthor.setText("Theo dõi");
            int primary = ContextCompat.getColor(this, R.color.primary);
            int white = ContextCompat.getColor(this, R.color.white);
            btnFollowAuthor.setBackgroundTintList(ColorStateList.valueOf(primary));
            btnFollowAuthor.setTextColor(white);
        }
    }
}
