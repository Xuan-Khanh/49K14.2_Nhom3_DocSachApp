package com.example.docsachapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docsachapp.adapter.StorySearchAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Story;
import com.example.docsachapp.model.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorProfileActivity extends AppCompatActivity {
    private boolean isFollowing = false;
    private int userId;
    private SessionManager sessionManager;
    
    private RoundedImageView ivAvatar;
    private TextView tvStoryCount, tvFollowerCount, tvFollowingCount;
    private TextView tvUsername, tvBio, tvStoriesHeader, tvHeaderUsername;
    private MaterialButton btnFollowAuthor;
    private RecyclerView rvAuthorStories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_profile);

        sessionManager = new SessionManager(this);
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
        tvHeaderUsername = findViewById(R.id.tv_header_username);
        btnFollowAuthor = findViewById(R.id.btn_follow_author);
        rvAuthorStories = findViewById(R.id.rv_author_stories);

        rvAuthorStories.setLayoutManager(new LinearLayoutManager(this));

        btnFollowAuthor.setOnClickListener(v -> toggleFollow());

        LinearLayout llFollowers = findViewById(R.id.ll_followers);
        LinearLayout llFollowing = findViewById(R.id.ll_following);

        llFollowers.setOnClickListener(v -> {
            Intent intent = new Intent(AuthorProfileActivity.this, FollowListActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", tvUsername.getText().toString());
            intent.putExtra("TAB_INITIAL", 0); // 0 for followers
            startActivity(intent);
        });

        llFollowing.setOnClickListener(v -> {
            Intent intent = new Intent(AuthorProfileActivity.this, FollowListActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", tvUsername.getText().toString());
            intent.putExtra("TAB_INITIAL", 1); // 1 for following
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        // Nếu đã đăng nhập, truyền token để biết mình có đang follow người này không
        String authHeader = sessionManager.getAuthHeader();
        
        RetrofitClient.getApi().getPublicProfile(userId, authHeader).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();
                    
                    tvUsername.setText(profile.getUsername());
                    if (tvHeaderUsername != null) tvHeaderUsername.setText(profile.getUsername());
                    tvStoryCount.setText(String.valueOf(profile.getStoryCount()));
                    tvFollowerCount.setText(String.valueOf(profile.getFollowerCount()));
                    tvFollowingCount.setText(String.valueOf(profile.getFollowingCount()));
                    tvBio.setText(profile.getBio() == null || profile.getBio().isEmpty() ? "Chưa có mô tả" : profile.getBio());

                    if (profile.getAvatar() != null && !profile.getAvatar().isEmpty()) {
                        String fullUrl = profile.getAvatar().startsWith("http") ? profile.getAvatar() 
                                         : "http://10.0.2.2:8000" + profile.getAvatar();
                        Glide.with(AuthorProfileActivity.this).load(fullUrl).placeholder(R.drawable.image5).into(ivAvatar);
                    } else {
                        ivAvatar.setImageResource(R.drawable.image5);
                    }

                    // ✅ FIX #9: Hiển thị cả truyện đang đăng VÀ hoàn thành (không lọc mất hoan_thanh)
                    List<Story> allStories = profile.getAllStories();
                    if (allStories != null && !allStories.isEmpty()) {
                        tvStoriesHeader.setText("Truyện đã đăng (" + allStories.size() + ")");
                        StorySearchAdapter adapter = new StorySearchAdapter(allStories, AuthorProfileActivity.this);
                        rvAuthorStories.setAdapter(adapter);
                    } else {
                        tvStoriesHeader.setText("Truyện đã đăng (0)");
                    }

                    isFollowing = profile.isFollowing();

                    // ✅ FIX #3: Nếu is_self = true → ẩn nút Follow (không cho follow chính mình)
                    if (profile.isSelf()) {
                        btnFollowAuthor.setVisibility(View.GONE);
                    } else {
                        btnFollowAuthor.setVisibility(View.VISIBLE);
                        updateFollowButtonUI();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Toast.makeText(AuthorProfileActivity.this, "Lỗi tải thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFollow() {
        String authHeader = sessionManager.getAuthHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Không cho tự follow chính mình
        if (userId == sessionManager.getUserId()) {
            Toast.makeText(this, "Bạn không thể theo dõi chính mình", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Integer> body = new HashMap<>();
        body.put("following_id", userId);

        if (isFollowing) {
            RetrofitClient.getApi().unfollowUser(authHeader, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        isFollowing = false;
                        updateFollowButtonUI();
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
            RetrofitClient.getApi().followUser(authHeader, body).enqueue(new Callback<Map<String, Object>>() {
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
