package com.example.docsachapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.adapter.StoryGridAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Collection;
import com.example.docsachapp.model.Story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddStoriesToCollectionActivity extends AppCompatActivity {

    private int collectionId;
    private RecyclerView rvStories;
    private StoryGridAdapter adapter;
    private List<Story> storyList = new ArrayList<>();
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stories_to_collection);

        collectionId = getIntent().getIntExtra("COLLECTION_ID", -1);
        if (collectionId == -1) {
            finish();
            return;
        }

        sessionManager = new SessionManager(this);
        initViews();
        loadCollectionAndStories();
    }

    private void initViews() {
        rvStories = findViewById(R.id.rv_add_stories);
        progressBar = findViewById(R.id.progress_bar);
        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView btnDone = findViewById(R.id.btn_done);

        btnBack.setOnClickListener(v -> finish());

        adapter = new StoryGridAdapter(storyList, this);
        adapter.setSelectionMode(true); // Bật chế độ chọn truyện
        rvStories.setLayoutManager(new GridLayoutManager(this, 3));
        rvStories.setAdapter(adapter);

        btnDone.setOnClickListener(v -> {
            Set<Integer> selectedIds = adapter.getSelectedStoryIds();
            if (selectedIds.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 truyện mới", Toast.LENGTH_SHORT).show();
            } else {
                addStoriesToCollection(selectedIds);
            }
        });
    }

    private void loadCollectionAndStories() {
        progressBar.setVisibility(View.VISIBLE);
        String token = sessionManager.getAuthHeader();

        // 1. Lấy chi tiết bộ sưu tập để biết truyện nào đã có sẵn
        RetrofitClient.getApi().getCollectionDetail(token, collectionId).enqueue(new Callback<Collection>() {
            @Override
            public void onResponse(Call<Collection> call, Response<Collection> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Set<Integer> existingIds = new HashSet<>();
                    if (response.body().getStories() != null) {
                        for (Story s : response.body().getStories()) {
                            existingIds.add(s.getId());
                        }
                    }
                    adapter.setAlreadyAddedIds(existingIds);
                }
                // 2. Sau đó mới tải danh sách truyện đang theo dõi
                loadFollowingStories();
            }

            @Override
            public void onFailure(Call<Collection> call, Throwable t) {
                loadFollowingStories();
            }
        });
    }

    private void loadFollowingStories() {
        String token = sessionManager.getAuthHeader();
        RetrofitClient.getApi().getFollowingStories(token).enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    storyList.clear();
                    storyList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddStoriesToCollectionActivity.this, "Lỗi tải danh sách truyện", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addStoriesToCollection(Set<Integer> storyIds) {
        String token = sessionManager.getAuthHeader();
        int total = storyIds.size();
        final int[] count = {0};

        for (Integer sId : storyIds) {
            Map<String, Object> body = new HashMap<>();
            body.put("collection_id", collectionId);
            body.put("story_id", sId);

            RetrofitClient.getApi().addStoryToCollection(token, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    count[0]++;
                    if (count[0] == total) {
                        Toast.makeText(AddStoriesToCollectionActivity.this, "Đã thêm thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    // Xử lý lỗi nếu cần
                }
            });
        }
    }
}