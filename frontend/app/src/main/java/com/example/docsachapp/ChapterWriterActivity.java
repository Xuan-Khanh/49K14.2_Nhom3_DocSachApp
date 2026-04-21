package com.example.docsachapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Chapter;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChapterWriterActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private TextView btnPublish, btnDraft;
    private int storyId = -1;
    private int chapterId = -1;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_writer);

        sessionManager = new SessionManager(this);
        storyId = getIntent().getIntExtra("STORY_ID", -1);
        chapterId = getIntent().getIntExtra("CHAPTER_ID", -1);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnPublish = findViewById(R.id.btn_publish);
        btnDraft = findViewById(R.id.btn_draft);
        etTitle = findViewById(R.id.et_chapter_title);
        etContent = findViewById(R.id.et_chapter_content);

        btnBack.setOnClickListener(v -> finish());

        if (chapterId != -1) {
            loadChapterDetail();
        }

        btnPublish.setOnClickListener(v -> saveChapter("da_dang"));
        btnDraft.setOnClickListener(v -> saveChapter("ban_nhap"));
    }

    private void loadChapterDetail() {
        RetrofitClient.getApi().getChapterDetail(chapterId).enqueue(new Callback<Chapter>() {
            @Override
            public void onResponse(@NonNull Call<Chapter> call, @NonNull Response<Chapter> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Chapter chapter = response.body();
                    etTitle.setText(chapter.getTitle());
                    etContent.setText(chapter.getContent());
                    
                    // Nếu đã đăng thì đổi màu nút
                    if (chapter.isPublished()) {
                        btnPublish.setTextColor(getResources().getColor(R.color.primary));
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<Chapter> call, @NonNull Throwable t) {
                Toast.makeText(ChapterWriterActivity.this, "Lỗi tải nội dung chương", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChapter(String status) {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("truyen", storyId);
        body.put("tieu_de", title);
        body.put("noi_dung", content);
        body.put("trang_thai", status);

        if (chapterId != -1) {
            RetrofitClient.getApi().updateChapter(chapterId, body).enqueue(new Callback<Chapter>() {
                @Override
                public void onResponse(@NonNull Call<Chapter> call, @NonNull Response<Chapter> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ChapterWriterActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ChapterWriterActivity.this, "Lỗi cập nhật (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(@NonNull Call<Chapter> call, @NonNull Throwable t) {
                    Toast.makeText(ChapterWriterActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            RetrofitClient.getApi().createChapter(body).enqueue(new Callback<Chapter>() {
                @Override
                public void onResponse(@NonNull Call<Chapter> call, @NonNull Response<Chapter> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ChapterWriterActivity.this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ChapterWriterActivity.this, "Lỗi khi lưu (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(@NonNull Call<Chapter> call, @NonNull Throwable t) {
                    Toast.makeText(ChapterWriterActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
