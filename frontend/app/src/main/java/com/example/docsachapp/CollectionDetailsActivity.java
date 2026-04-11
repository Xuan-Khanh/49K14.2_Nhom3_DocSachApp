package com.example.docsachapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.adapter.StoryVerticalAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Collection;
import com.example.docsachapp.model.Story;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CollectionDetailsActivity extends AppCompatActivity {

    private int collectionId;
    private RecyclerView rvStories;
    private StoryVerticalAdapter adapter;
    private List<Story> storyList = new ArrayList<>();
    
    private TextView tvName, tvCount;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_details);

        collectionId = getIntent().getIntExtra("COLLECTION_ID", -1);
        if (collectionId == -1) {
            finish();
            return;
        }

        sessionManager = new SessionManager(this);
        initViews();
        loadCollectionDetails();
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_collection_name);
        tvCount = findViewById(R.id.tv_book_count);
        rvStories = findViewById(R.id.rv_stories);
        progressBar = findViewById(R.id.progress_bar);
        
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        adapter = new StoryVerticalAdapter(storyList, this);
        rvStories.setLayoutManager(new LinearLayoutManager(this));
        rvStories.setAdapter(adapter);
    }

    private void loadCollectionDetails() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        String token = sessionManager.getAuthHeader();

        RetrofitClient.getApi().getCollectionDetail(token, collectionId).enqueue(new Callback<Collection>() {
            @Override
            public void onResponse(Call<Collection> call, Response<Collection> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Collection collection = response.body();
                    
                    tvName.setText(collection.getName());
                    tvCount.setText(collection.getStoryCount() + " truyện");
                    
                    storyList.clear();
                    if (collection.getStories() != null) {
                        storyList.addAll(collection.getStories());
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CollectionDetailsActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Collection> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(CollectionDetailsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}