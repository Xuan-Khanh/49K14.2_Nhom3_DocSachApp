package com.example.docsachapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recently_read_stories);

        sessionManager = new SessionManager(this);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        rvStories = findViewById(R.id.rv_recently_read);
        progressBar = new ProgressBar(this); // Trong layout thực tế nên có ProgressBar
        
        rvStories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReadingHistoryAdapter(historyList, this);
        rvStories.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;

        RetrofitClient.getApi().getReadingHistory(token).enqueue(new Callback<List<ReadingHistoryItem>>() {
            @Override
            public void onResponse(Call<List<ReadingHistoryItem>> call, Response<List<ReadingHistoryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    historyList.clear();
                    historyList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<ReadingHistoryItem>> call, Throwable t) {
            }
        });
    }
}
