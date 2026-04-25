package com.example.docsachapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.docsachapp.adapter.ReadingHistoryAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.ReadingHistoryItem;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecentlyReadStoriesActivity extends AppCompatActivity {
    private RecyclerView rvStories;
    private ReadingHistoryAdapter adapter;
    private List<ReadingHistoryItem> historyList = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recently_read_stories);

        sessionManager = new SessionManager(this);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        rvStories = findViewById(R.id.rv_recently_read);
        rvStories.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new ReadingHistoryAdapter(historyList, this, true);
        rvStories.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        // Lấy Token theo định dạng "Token [giá trị]"
        String authHeader = sessionManager.getAuthHeader();
        
        if (authHeader == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApi().getReadingHistory(authHeader).enqueue(new Callback<List<ReadingHistoryItem>>() {
            @Override
            public void onResponse(Call<List<ReadingHistoryItem>> call, Response<List<ReadingHistoryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    historyList.clear();
                    historyList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(RecentlyReadStoriesActivity.this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ReadingHistoryItem>> call, Throwable t) {
                Toast.makeText(RecentlyReadStoriesActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
