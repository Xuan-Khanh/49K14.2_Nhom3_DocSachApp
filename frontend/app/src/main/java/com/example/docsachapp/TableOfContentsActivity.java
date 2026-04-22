package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.adapter.ChapterAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Chapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TableOfContentsActivity extends AppCompatActivity {

    private int storyId;
    private String storyTitle;
    private RecyclerView rvChapters;
    private ChapterAdapter adapter;
    private List<Chapter> chapterList = new ArrayList<>();
    private TextView tvStoryTitle, tvPageNumber;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_of_contents);

        sessionManager = new SessionManager(this);
        storyId = getIntent().getIntExtra("STORY_ID", -1);
        storyTitle = getIntent().getStringExtra("STORY_TITLE");

        if (storyId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadChapters();
    }

    private void initViews() {
        tvStoryTitle = findViewById(R.id.tv_story_title);
        tvStoryTitle.setText(storyTitle != null ? storyTitle : "Mục lục");

        rvChapters = findViewById(R.id.rv_chapters);
        rvChapters.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChapterAdapter(chapterList, this, chapter -> {
            // Chuyển sang màn hình đọc truyện
            Intent intent = new Intent(TableOfContentsActivity.this, ReadingActivity.class);
            intent.putExtra("STORY_ID", storyId);
            intent.putExtra("CHAPTER_ID", chapter.getId());
            startActivity(intent);
        });
        rvChapters.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        tvPageNumber = findViewById(R.id.tv_page_number);
        // Tạm thời chưa xử lý phân trang phức tạp, chỉ hiện 1/1 hoặc số lượng chương
    }

    private void loadChapters() {
        String token = sessionManager.getAuthHeader();
        RetrofitClient.getApi().getChapters(token, storyId).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(Call<List<Chapter>> call, Response<List<Chapter>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chapterList.clear();
                    chapterList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    // Cập nhật số trang (giả định)
                    tvPageNumber.setText("1/1"); 
                } else {
                    Toast.makeText(TableOfContentsActivity.this, "Không thể tải danh sách chương", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Chapter>> call, Throwable t) {
                Toast.makeText(TableOfContentsActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
