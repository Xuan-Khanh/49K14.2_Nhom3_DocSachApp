package com.example.docsachapp;

import android.os.Bundle;
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

// Màn hình hiển thị danh sách các Truyện Mới Cập Nhật (được mở khi bấm "Xem thêm" ở trang chủ)
public class UpdateStoriesActivity extends AppCompatActivity {
    private RecyclerView rvStories;
    private StoryVerticalAdapter adapter;
    private List<Story> storyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_stories);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        rvStories = findViewById(R.id.rv_recently_read);
        rvStories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StoryVerticalAdapter(storyList, this);
        rvStories.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        // Đồng bộ với trang chủ: Sử dụng API getRecentlyUpdated để lấy danh sách truyện mới có chương mới
        RetrofitClient.getApi().getRecentlyUpdated().enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    storyList.clear();
                    storyList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {
                Toast.makeText(UpdateStoriesActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
