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
    private String currentStatus = "ban_thao";
    private String originalTitle = "";
    private String originalContent = "";
    private TextView tvToolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_writer);

        sessionManager = new SessionManager(this);
        storyId = getIntent().getIntExtra("STORY_ID", -1);
        chapterId = getIntent().getIntExtra("CHAPTER_ID", -1);

        ImageView btnBack = findViewById(R.id.btn_back);
        tvToolbarTitle = findViewById(R.id.tv_title);
        btnPublish = findViewById(R.id.btn_publish);
        btnDraft = findViewById(R.id.btn_draft);
        etTitle = findViewById(R.id.et_chapter_title);
        etContent = findViewById(R.id.et_chapter_content);

        btnBack.setOnClickListener(v -> finish());

        if (chapterId != -1) {
            loadChapterDetail();
        } else {
            btnDraft.setText("NHÁP");
            btnPublish.setText("ĐĂNG");
            btnPublish.setOnClickListener(v -> saveChapter("da_dang"));
            btnDraft.setOnClickListener(v -> saveChapter("ban_thao"));
        }

        setupTextWatchers();
        updateButtonStates();
    }

    private void loadChapterDetail() {
        String token = sessionManager.getAuthHeader();
        RetrofitClient.getApi().getChapterDetail(token, chapterId).enqueue(new Callback<Chapter>() {
            @Override
            public void onResponse(@NonNull Call<Chapter> call, @NonNull Response<Chapter> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Chapter chapter = response.body();
                    originalTitle = chapter.getTitle();
                    originalContent = chapter.getContent();
                    currentStatus = chapter.getStatus();
                    
                    tvToolbarTitle.setText(originalTitle);

                    if (chapter.isPublished()) {
                        btnDraft.setText("XÓA");
                        btnPublish.setText("LƯU");
                        btnDraft.setOnClickListener(v -> confirmDeleteChapter());
                        btnPublish.setOnClickListener(v -> saveChapter(currentStatus));
                    } else {
                        btnDraft.setText("NHÁP");
                        btnPublish.setText("ĐĂNG");
                        btnDraft.setOnClickListener(v -> saveChapter("ban_thao"));
                        btnPublish.setOnClickListener(v -> saveChapter("da_dang"));
                    }

                    etTitle.setText(originalTitle);
                    etContent.setText(originalContent);
                    updateButtonStates();
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
        body.put("truyen_id", storyId);
        body.put("tieu_de", title);
        body.put("noi_dung", content);
        body.put("trang_thai", status);

        String token = sessionManager.getAuthHeader();
        if (chapterId != -1) {
            RetrofitClient.getApi().updateChapter(token, chapterId, body).enqueue(new Callback<Chapter>() {
                @Override
                public void onResponse(@NonNull Call<Chapter> call, @NonNull Response<Chapter> response) {
                    if (response.isSuccessful()) {
                        if ("ban_thao".equals(status)) {
                            finish();
                        } else if ("da_dang".equals(status) && "ban_thao".equals(currentStatus)) {
                            showCustomToast("Đã đăng thành công " + title);
                            finish();
                        } else {
                            showCustomToast("Đã cập nhật thành công " + title);
                            finish();
                        }
                    } else {
                        Toast.makeText(ChapterWriterActivity.this, "Lỗi cập nhật (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(@NonNull Call<Chapter> call, @NonNull Throwable t) {
                    Toast.makeText(ChapterWriterActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            RetrofitClient.getApi().createChapter(token, body).enqueue(new Callback<Chapter>() {
                @Override
                public void onResponse(@NonNull Call<Chapter> call, @NonNull Response<Chapter> response) {
                    if (response.isSuccessful()) {
                        if ("ban_thao".equals(status)) {
                            finish();
                        } else {
                            showCustomToast("Đã đăng thành công " + title);
                            finish();
                        }
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

    private void setupTextWatchers() {
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                updateButtonStates();
            }
        };
        etTitle.addTextChangedListener(watcher);
        etContent.addTextChangedListener(watcher);
    }

    private void updateButtonStates() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        boolean hasText = !title.isEmpty() && !content.isEmpty();

        boolean isEditMode = btnDraft.getText().toString().equalsIgnoreCase("XÓA");

        if (isEditMode) {
            boolean hasChanged = !title.equals(originalTitle) || !content.equals(originalContent);
            btnDraft.setEnabled(true);
            btnDraft.setTextColor(getResources().getColor(R.color.text_dark));
            
            if (hasText && hasChanged) {
                btnPublish.setEnabled(true);
                btnPublish.setTextColor(getResources().getColor(R.color.primary));
            } else {
                btnPublish.setEnabled(false);
                btnPublish.setTextColor(android.graphics.Color.parseColor("#999999"));
            }
        } else {
            if (hasText) {
                btnDraft.setEnabled(true);
                btnDraft.setTextColor(getResources().getColor(R.color.text_dark));
                btnPublish.setEnabled(true);
                btnPublish.setTextColor(getResources().getColor(R.color.primary));
            } else {
                btnDraft.setEnabled(false);
                btnDraft.setTextColor(android.graphics.Color.parseColor("#999999"));
                btnPublish.setEnabled(false);
                btnPublish.setTextColor(android.graphics.Color.parseColor("#999999"));
            }
        }
    }

    private void confirmDeleteChapter() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Xóa chương")
            .setMessage("Bạn có chắc chắn muốn xóa chương này?")
            .setPositiveButton("Xóa", (dialog, which) -> deleteChapter())
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteChapter() {
        String token = sessionManager.getAuthHeader();
        RetrofitClient.getApi().deleteChapter(token, chapterId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    showCustomToast("Đã xóa thành công " + originalTitle);
                    finish();
                } else {
                    Toast.makeText(ChapterWriterActivity.this, "Lỗi khi xóa (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Toast.makeText(ChapterWriterActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCustomToast(String message) {
        android.view.View layout = getLayoutInflater().inflate(R.layout.custom_toast_success, null);
        TextView tvMessage = layout.findViewById(R.id.tv_toast_message);
        tvMessage.setText(message);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
