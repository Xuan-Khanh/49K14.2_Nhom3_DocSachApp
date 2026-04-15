package com.example.docsachapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.docsachapp.adapter.StoryVerticalAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.model.Story;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostStoriesActivity extends AppCompatActivity {
    private RecyclerView rvStories;
    private StoryVerticalAdapter adapter;
    private List<Story> storyList = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_stories);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        rvStories = findViewById(R.id.rv_recently_read);
        progressBar = findViewById(R.id.progress_bar); // Đảm bảo ID này tồn tại hoặc xóa dòng này
        
        rvStories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StoryVerticalAdapter(storyList, this);
        rvStories.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        // Gọi API lấy tất cả truyện, server thường trả về truyện mới nhất trước
        RetrofitClient.getApi().getStories(null, null, null).enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    storyList.clear();
                    storyList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(PostStoriesActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {
                Toast.makeText(PostStoriesActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
