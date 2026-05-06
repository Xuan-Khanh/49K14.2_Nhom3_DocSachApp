package com.example.docsachapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Chapter;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ReadingActivity – Màn hình đọc truyện
 *
 * Luồng:
 * 1. Nhận STORY_ID từ BookDetailsActivity
 * 2. Gọi API GET /api/stories/{id}/chapters để lấy danh sách chương
 * 3. Mặc định mở chương đầu tiên (hoặc chương được truyền qua CHAPTER_ID)
 * 4. Gọi API GET /api/chapters/{id} để lấy nội dung chương
 * 5. Điều hướng Trước / Sau / Đầu / Cuối bằng danh sách đã load
 * 6. Gọi API POST /api/reading-history/update để cập nhật lịch sử đọc
 */
public class ReadingActivity extends AppCompatActivity {

    private int storyId = -1;
    private int currentIndex = 0;
    private List<Chapter> chapterList = new ArrayList<>();

    // IDs từ activity_reading.xml
    private TextView tvChapterTitle;     // tv_chapter_title_large
    private TextView tvContent;          // tv_content
    private TextView tvHeaderMeta;       // tv_header_meta (số chương / ngày)
    private TextView tvHeaderTitle;      // tv_header_title
    private TextView btnFirst, btnPrev, btnNext, btnLast;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        sessionManager = new SessionManager(this);
        storyId = getIntent().getIntExtra("STORY_ID", -1);
        int requestedChapterId = getIntent().getIntExtra("CHAPTER_ID", -1);

        if (storyId == -1) {
            Toast.makeText(this, "Không xác định được truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadChapters(requestedChapterId);
    }

    private void initViews() {
        ImageView btnBack         = findViewById(R.id.btn_back);
        tvChapterTitle = findViewById(R.id.tv_chapter_title_large); // tiêu đề chương lớn
        tvContent      = findViewById(R.id.tv_content);              // nội dung chương
        tvHeaderTitle  = findViewById(R.id.tv_header_title);          // header title ngắn
        tvHeaderMeta   = findViewById(R.id.tv_header_meta);           // số chương / ngày
        btnFirst       = findViewById(R.id.btn_first);
        btnPrev        = findViewById(R.id.btn_prev);
        btnNext        = findViewById(R.id.btn_next);
        btnLast        = findViewById(R.id.btn_last);

        btnBack.setOnClickListener(v -> finish());

        btnFirst.setOnClickListener(v -> { if (!chapterList.isEmpty()) goToChapter(0); });
        btnPrev.setOnClickListener(v  -> { if (currentIndex > 0) goToChapter(currentIndex - 1); });
        btnNext.setOnClickListener(v  -> { if (currentIndex < chapterList.size() - 1) goToChapter(currentIndex + 1); });
        btnLast.setOnClickListener(v  -> { if (!chapterList.isEmpty()) goToChapter(chapterList.size() - 1); });

        View btnSelectChapter = findViewById(R.id.btn_select_chapter);
        if (btnSelectChapter != null) {
            btnSelectChapter.setOnClickListener(v -> showChapterPicker());
        }
    }

    // ─── Bước 1: Load danh sách chương ──────────────────────────────────────
    private void loadChapters(int requestedChapterId) {
        showLoading(true);

        String token = sessionManager.getAuthHeader();
        RetrofitClient.getApi().getChapters(token, storyId).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(Call<List<Chapter>> call, Response<List<Chapter>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    chapterList.clear();
                    chapterList.addAll(response.body());

                    // Tìm chương được yêu cầu, mặc định là chương đầu
                    int startIndex = 0;
                    if (requestedChapterId != -1) {
                        for (int i = 0; i < chapterList.size(); i++) {
                            if (chapterList.get(i).getId() == requestedChapterId) {
                                startIndex = i;
                                break;
                            }
                        }
                    }
                    goToChapter(startIndex);
                } else {
                    showLoading(false);
                    Toast.makeText(ReadingActivity.this,
                            "Truyện chưa có chương nào được đăng", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Chapter>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ReadingActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Bước 2: Đọc nội dung chương ─────────────────────────────────────────
    private void goToChapter(int index) {
        if (index < 0 || index >= chapterList.size()) return;
        currentIndex = index;

        Chapter chapter = chapterList.get(index);
        showLoading(true);

        // Gọi API lấy nội dung chi tiết của Chương (Content)
        String token = sessionManager.getAuthHeader();
        RetrofitClient.getApi().getChapterDetail(token, chapter.getId()).enqueue(new Callback<Chapter>() {
            @Override
            public void onResponse(Call<Chapter> call, Response<Chapter> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    displayChapter(response.body());
                    updateNavigationButtons();
                    updateReadingHistory(chapter.getId());
                } else {
                    Toast.makeText(ReadingActivity.this,
                            "Không thể tải nội dung chương", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Chapter> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ReadingActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hiển thị nội dung chương lên giao diện
    private void displayChapter(Chapter chapter) {
        String progressText = (currentIndex + 1) + " / " + chapterList.size();

        if (tvChapterTitle != null) tvChapterTitle.setText(chapter.getTitle());
        if (tvHeaderTitle  != null) tvHeaderTitle.setText(chapter.getTitle());
        if (tvHeaderMeta   != null) tvHeaderMeta.setText(progressText);
        if (tvContent      != null) tvContent.setText(
                chapter.getContent().isEmpty() ? "Đang cập nhật nội dung..." : chapter.getContent());

        // Đặt số chương hiện tại trong dropdown
        TextView tvCurrent = findViewById(R.id.tv_current_chapter);
        if (tvCurrent != null) tvCurrent.setText(String.valueOf(currentIndex + 1));
    }

    // Cập nhật trạng thái bật/tắt (Enable/Disable) của các nút điều hướng (Trước/Sau)
    private void updateNavigationButtons() {
        boolean isFirst = (currentIndex == 0);
        boolean isLast  = (currentIndex == chapterList.size() - 1);

        btnFirst.setEnabled(!isFirst);
        btnPrev.setEnabled(!isFirst);
        btnNext.setEnabled(!isLast);
        btnLast.setEnabled(!isLast);

        btnFirst.setAlpha(isFirst ? 0.4f : 1f);
        btnPrev.setAlpha(isFirst  ? 0.4f : 1f);
        btnNext.setAlpha(isLast   ? 0.4f : 1f);
        btnLast.setAlpha(isLast   ? 0.4f : 1f);
    }

    // ─── Bước 3: Lưu lịch sử đọc ─────────────────────────────────────────────
    private void updateReadingHistory(int chapterId) {
        String token = sessionManager.getAuthHeader();
        if (token == null) return; // Chưa đăng nhập → bỏ qua

        Map<String, Object> body = new HashMap<>();
        body.put("story_id", storyId);
        body.put("chapter_id", chapterId);

        RetrofitClient.getApi().updateReadingHistory(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {}
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    // ─── Hiện dialog chọn chương ─────────────────────────────────────────────
    private void showChapterPicker() {
        if (chapterList.isEmpty()) return;

        String[] titles = new String[chapterList.size()];
        for (int i = 0; i < chapterList.size(); i++) {
            titles[i] = (i + 1) + ". " + chapterList.get(i).getTitle();
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Chọn chương")
                .setItems(titles, (dialog, which) -> goToChapter(which))
                .show();
    }

    private void showLoading(boolean loading) {
        // Layout không có progress_bar ringêng — tạm hiện/ẩn content qua tv_content
        if (tvContent != null) {
            tvContent.setText(loading ? "Đang tải..." : (tvContent.getText().length() > 7 ? tvContent.getText() : ""));
        }
        // Vô hiệu hóa các nút khi đang tải
        boolean enable = !loading;
        if (btnFirst != null) btnFirst.setEnabled(enable);
        if (btnPrev  != null) btnPrev.setEnabled(enable);
        if (btnNext  != null) btnNext.setEnabled(enable);
        if (btnLast  != null) btnLast.setEnabled(enable);
    }
}
