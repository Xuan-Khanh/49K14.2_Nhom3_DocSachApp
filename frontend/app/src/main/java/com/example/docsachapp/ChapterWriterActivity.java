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

// Màn hình Soạn thảo Chương (dùng chung cho cả việc Viết chương mới và Sửa/Xóa chương cũ)
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
        // Lấy ID truyện và ID chương (nếu có) được truyền từ màn hình trước sang
        // Nếu chapterId = -1 nghĩa là người dùng đang tạo chương mới
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
            // Chế độ: CHỈNH SỬA chương cũ -> Gọi API tải dữ liệu gốc về
            loadChapterDetail();
        } else {
            // Chế độ: TẠO MỚI chương -> Setup giao diện các nút mặc định
            btnDraft.setText("NHÁP");
            btnPublish.setText("ĐĂNG");
            btnPublish.setOnClickListener(v -> saveChapter("da_dang"));
            btnDraft.setOnClickListener(v -> saveChapter("ban_thao"));
        }

        setupTextWatchers();
        updateButtonStates();
    }

    // Lấy nội dung chi tiết của chương từ Server (Chỉ chạy khi đang ở chế độ Chỉnh sửa chương cũ)
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
                        // Nếu chương này ĐÃ TỪNG ĐƯỢC XUẤT BẢN:
                        // Đổi tên nút thành "XÓA" và "LƯU" thay vì Nháp/Đăng
                        btnDraft.setText("XÓA");
                        btnPublish.setText("LƯU");
                        btnDraft.setOnClickListener(v -> confirmDeleteChapter());
                        btnPublish.setOnClickListener(v -> saveChapter(currentStatus));
                    } else {
                        // Nếu chương này vẫn đang là BẢN NHÁP:
                        // Giữ nguyên giao diện nút NHÁP / ĐĂNG
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

    // Gửi dữ liệu lên Server (Tạo mới hoặc Cập nhật tùy vào việc đã có ID chương hay chưa)
    private void saveChapter(String status) {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đóng gói dữ liệu gửi lên Backend thành một đối tượng Body
        Map<String, Object> body = new HashMap<>();
        body.put("truyen_id", storyId);
        body.put("tieu_de", title);
        body.put("noi_dung", content);
        body.put("trang_thai", status);

        String token = sessionManager.getAuthHeader();
        if (chapterId != -1) {
            // TRƯỜNG HỢP CẬP NHẬT: Gọi API updateChapter và truyền kèm theo ID của chương
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

    // Cập nhật trạng thái Bật/Tắt (Màu sắc) của các nút bấm dựa trên việc người dùng đã nhập chữ hay chưa
    private void updateButtonStates() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        
        // Kiểm tra xem người dùng đã nhập CẢ Tiêu đề VÀ Nội dung chưa?
        boolean hasText = !title.isEmpty() && !content.isEmpty();

        // Kiểm tra xem hiện tại nút Draft đang hiện chữ "XÓA" (nghĩa là đang ở Chế độ Sửa) hay "NHÁP" (Tạo mới)
        boolean isEditMode = btnDraft.getText().toString().equalsIgnoreCase("XÓA");

        if (isEditMode) {
            // Chế độ Sửa: Phải kiểm tra xem người dùng CÓ THỰC SỰ SỬA CHỮ nào so với nội dung gốc tải từ server không
            boolean hasChanged = !title.equals(originalTitle) || !content.equals(originalContent);
            
            btnDraft.setEnabled(true); // Nút XÓA thì lúc nào cũng bấm được
            btnDraft.setTextColor(getResources().getColor(R.color.text_dark));
            
            // Nút LƯU chỉ sáng lên nếu: Đã điền đủ chữ VÀ Có sự thay đổi nội dung
            if (hasText && hasChanged) {
                btnPublish.setEnabled(true);
                btnPublish.setTextColor(getResources().getColor(R.color.primary));
            } else {
                btnPublish.setEnabled(false);
                btnPublish.setTextColor(android.graphics.Color.parseColor("#999999"));
            }
        } else {
            // Chế độ Tạo mới: Chỉ cần điền đủ nội dung là cả 2 nút NHÁP và ĐĂNG đều sáng lên
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

    // Gọi API xóa hoàn toàn chương này
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
