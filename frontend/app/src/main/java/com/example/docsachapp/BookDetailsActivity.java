package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Collection;
import com.example.docsachapp.model.Story;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Màn hình Xem chi tiết của một cuốn truyện (Đánh giá, Theo dõi, Thông số lượt đọc...)
public class BookDetailsActivity extends AppCompatActivity {
    private int storyId;
    private int authorId = -1;
    private SessionManager sessionManager;
    private boolean isFollowing = false;
    private int myRating = 0; 
    
    private TextView tvTitle, tvAuthor, tvDescription, tvRating, tvRatingCount;
    private TextView tvStatus, tvTime, tvViewsCount, tvChaptersCount, tvFollowersCount;
    private RoundedImageView ivCover, ivAuthorAvatar;
    private ChipGroup cgGenres;
    private MaterialButton btnFollow;
    private ImageView[] stars = new ImageView[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        sessionManager = new SessionManager(this);
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
        tvRatingCount = findViewById(R.id.tv_rating_count);
        ivCover = findViewById(R.id.iv_book_cover);
        ivAuthorAvatar = findViewById(R.id.iv_author_avatar);
        cgGenres = findViewById(R.id.cg_genres);
        tvStatus = findViewById(R.id.tv_story_status);
        tvTime = findViewById(R.id.tv_story_time);
        
        // Ánh xạ 3 con số thực tế
        tvViewsCount = findViewById(R.id.tv_views_count);
        tvFollowersCount = findViewById(R.id.tv_followers_count);
        tvChaptersCount = findViewById(R.id.tv_chapters_count);
        
        btnFollow = findViewById(R.id.btn_follow_book);
        
        stars[0] = findViewById(R.id.iv_star1);
        stars[1] = findViewById(R.id.iv_star2);
        stars[2] = findViewById(R.id.iv_star3);
        stars[3] = findViewById(R.id.iv_star4);
        stars[4] = findViewById(R.id.iv_star5);

        for (int i = 0; i < 5; i++) {
            final int score = i + 1;
            stars[i].setOnClickListener(v -> handleRatingClick(score));
        }
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        View.OnClickListener authorClick = v -> {
            if (authorId != -1) {
                Intent intent = new Intent(this, AuthorProfileActivity.class);
                intent.putExtra("USER_ID", authorId);
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

        findViewById(R.id.btn_toc).setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailsActivity.this, TableOfContentsActivity.class);
            intent.putExtra("STORY_ID", storyId);
            intent.putExtra("STORY_TITLE", tvTitle.getText().toString());
            startActivity(intent);
        });

        findViewById(R.id.btn_collection).setOnClickListener(v -> showAddToCollectionDialog());
        
        if (btnFollow != null) {
            btnFollow.setOnClickListener(v -> toggleFollow());
        }
    }

    // Xử lý sự kiện bấm vào các ngôi sao để Gửi đánh giá
    private void handleRatingClick(int score) {
        String token = sessionManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        myRating = score;
        updateStarsUI(myRating);
        Map<String, Object> body = new HashMap<>();
        body.put("story_id", storyId);
        body.put("rating", score);
        RetrofitClient.getApi().postRating(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookDetailsActivity.this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    loadStoryDetails();
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(BookDetailsActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStarsUI(int score) {
        for (int i = 0; i < 5; i++) {
            if (stars[i] == null) continue;
            if (i < score) {
                stars[i].setImageResource(android.R.drawable.btn_star_big_on);
                stars[i].setColorFilter(ContextCompat.getColor(this, R.color.published_yellow));
            } else {
                stars[i].setImageResource(android.R.drawable.btn_star_big_off);
                stars[i].setColorFilter(ContextCompat.getColor(this, R.color.placeholder));
            }
        }
    }

    // Gọi API Theo dõi / Bỏ theo dõi truyện này
    private void toggleFollow() {
        String token = sessionManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("story_id", storyId);
        if (!isFollowing) {
            RetrofitClient.getApi().followStory(token, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        isFollowing = true;
                        updateFollowButtonUI();
                        loadStoryDetails(); // Tải lại để cập nhật số followers thực tế
                    }
                }
                @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
            });
        } else {
            RetrofitClient.getApi().unfollowStory(token, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        isFollowing = false;
                        updateFollowButtonUI();
                        loadStoryDetails(); // Tải lại để cập nhật số followers thực tế
                    }
                }
                @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
            });
        }
    }

    private void updateFollowButtonUI() {
        if (btnFollow == null) return;
        if (isFollowing) {
            btnFollow.setText("BỎ THEO DÕI");
            btnFollow.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_light));
            btnFollow.setIconResource(android.R.drawable.btn_star_big_on);
        } else {
            btnFollow.setText("THEO DÕI TRUYỆN");
            btnFollow.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
            btnFollow.setIconResource(android.R.drawable.btn_star_big_off);
        }
    }

    // Bật BottomSheet (Bảng vuốt từ dưới màn hình lên) để chọn lưu truyện vào 1 Bộ sưu tập có sẵn
    private void showAddToCollectionDialog() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_to_collection, null);
        bottomSheetDialog.setContentView(dialogView);
        RadioGroup rgCollections = dialogView.findViewById(R.id.rg_collections);
        RetrofitClient.getApi().getBoSuuTap(token).enqueue(new Callback<List<Collection>>() {
            @Override
            public void onResponse(Call<List<Collection>> call, Response<List<Collection>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rgCollections.removeAllViews();
                    for (Collection collection : response.body()) {
                        RadioButton rb = (RadioButton) LayoutInflater.from(BookDetailsActivity.this).inflate(R.layout.item_collection_radio, rgCollections, false);
                        rb.setId(collection.getId());
                        rb.setText(collection.getName());
                        rgCollections.addView(rb);
                    }
                }
            }
            @Override public void onFailure(Call<List<Collection>> call, Throwable t) {}
        });
        dialogView.findViewById(R.id.tv_done).setOnClickListener(v -> {
            int selectedId = rgCollections.getCheckedRadioButtonId();
            if (selectedId != -1) {
                Map<String, Object> body = new HashMap<>();
                body.put("collection_id", selectedId);
                body.put("story_id", storyId);
                RetrofitClient.getApi().addStoryToCollection(token, body).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(BookDetailsActivity.this, "Thêm vào BST thành công", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        }
                    }
                    @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
                });
            }
        });
        bottomSheetDialog.show();
    }

    private void loadStoryDetails() {
        String token = sessionManager.getAuthHeader();
        RetrofitClient.getApi().getStoryDetail(token, storyId).enqueue(new Callback<Story>() {
            @Override
            public void onResponse(Call<Story> call, Response<Story> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Story story = response.body();
                    if (story.getAuthor() != null) authorId = story.getAuthor().getId();
                    isFollowing = story.isFollowing();
                    myRating = story.getUserRating();
                    updateFollowButtonUI();
                    displayStory(story);
                }
            }
            @Override
            public void onFailure(Call<Story> call, Throwable t) {
                Toast.makeText(BookDetailsActivity.this, "Lỗi tải thông tin truyện", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayStory(Story story) {
        tvTitle.setText(story.getTitle());
        tvAuthor.setText(story.getAuthorName());
        tvDescription.setText(story.getDescription());
        if (tvStatus != null) tvStatus.setText(story.getStatusDisplay());
        if (tvTime != null) {
            String timeText = story.getFormattedUpdatedAt();
            tvTime.setText(timeText.isEmpty() ? "" : "· " + timeText);
        }

        // ✅ HIỂN THỊ DỮ LIỆU THẬT
        if (tvViewsCount != null) tvViewsCount.setText("👁 " + formatCount(story.getViews()));
        if (tvFollowersCount != null) tvFollowersCount.setText("  🔖 " + formatCount(story.getFollowerCount()));
        if (tvChaptersCount != null) tvChaptersCount.setText("  ≡ " + story.getChaptersCount() + " chương");
        
        if (tvRating != null) tvRating.setText(String.format("%.1f/5.0", story.getRating()));
        if (tvRatingCount != null) tvRatingCount.setText(story.getTotalRatings() + " người đánh giá");
        
        if (myRating > 0) updateStarsUI(myRating);
        else updateStarsUI((int) Math.round(story.getRating()));

        Glide.with(this).load(story.getCoverUrl()).placeholder(R.drawable.biatruyen).into(ivCover);
        if (ivAuthorAvatar != null && story.getAuthor() != null && story.getAuthor().getAvatar() != null) {
            Glide.with(this).load(story.getAuthor().getAvatar()).circleCrop().into(ivAuthorAvatar);
        }
    }

    private String formatCount(int count) {
        if (count >= 1000000) return String.format("%.1f Tr", count / 1000000.0);
        if (count >= 1000) return String.format("%.1f N", count / 1000.0);
        return String.valueOf(count);
    }
}
