package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Story;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Màn hình Bước 2: Hoàn tất thông tin phụ và gọi API lên Server để tạo truyện
public class BookCreateStep2Activity extends AppCompatActivity {
    private static final String TAG = "BookCreateStep2";
    private SessionManager sessionManager;
    private String bookTitle = "";
    private String bookDescription = "";
    private int createdStoryId = -1; // ID truyện sau khi tạo thành công

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_create_step2);

        sessionManager = new SessionManager(this);

        // Nhận dữ liệu từ Step 1
        bookTitle = getIntent().getStringExtra("BOOK_TITLE");
        bookDescription = getIntent().getStringExtra("BOOK_DESCRIPTION");

        if (bookTitle == null) bookTitle = "";
        if (bookDescription == null) bookDescription = "";

        // Hiển thị tiêu đề & mô tả đã nhập ở step 1
        TextView tvBookTitle = findViewById(R.id.tv_book_title);
        TextView tvBookDesc = findViewById(R.id.tv_book_desc);
        tvBookTitle.setText(bookTitle);
        tvBookDesc.setText(bookDescription);

        ImageView btnClose = findViewById(R.id.btn_close);
        ImageView btnDone = findViewById(R.id.btn_done);
        TextView btnAddChapter = findViewById(R.id.btn_add_chapter);

        btnClose.setOnClickListener(v -> finish());

        // Nút bấm Hoàn thành -> Gọi API lưu truyện rồi đóng màn hình
        btnDone.setOnClickListener(v -> {
            if (createdStoryId != -1) {
                // Truyện đã được tạo rồi, chỉ cần finish
                Toast.makeText(this, "Thêm truyện thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Tạo truyện mới rồi finish
                createStoryAndFinish();
            }
        });

        btnAddChapter.setOnClickListener(v -> {
            if (createdStoryId != -1) {
                // Truyện đã được tạo → chuyển thẳng sang thêm chương
                navigateToChapterWriter(createdStoryId);
            } else {
                // Tạo truyện trước, sau đó chuyển sang thêm chương
                createStoryThenAddChapter();
            }
        });
    }

    /**
     * Gửi API tạo truyện mới lên server, thành công thì đóng màn hình kết thúc tiến trình
     */
    private void createStoryAndFinish() {
        String token = sessionManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy trạng thái từ switch (nếu có)
        String statusStr = "da_dang";
        Switch switchStatus = findViewById(R.id.switch_status);
        if (switchStatus != null && switchStatus.isChecked()) {
            statusStr = "hoan_thanh";
        }

        RequestBody title = RequestBody.create(MediaType.parse("text/plain"), bookTitle);
        RequestBody desc = RequestBody.create(MediaType.parse("text/plain"), bookDescription);
        RequestBody status = RequestBody.create(MediaType.parse("text/plain"), statusStr);

        RetrofitClient.getApi().createStory(token, title, desc, status, new java.util.ArrayList<>(), null)
                .enqueue(new Callback<Story>() {
                    @Override
                    public void onResponse(@NonNull Call<Story> call, @NonNull Response<Story> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(BookCreateStep2Activity.this, "Thêm truyện thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.e(TAG, "Create story failed: " + response.code());
                            Toast.makeText(BookCreateStep2Activity.this, "Lỗi tạo truyện (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Story> call, @NonNull Throwable t) {
                        Log.e(TAG, "Create story error", t);
                        Toast.makeText(BookCreateStep2Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Tạo truyện mới, sau đó chuyển sang màn hình thêm chương
     */
    private void createStoryThenAddChapter() {
        String token = sessionManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy trạng thái từ switch (nếu có)
        String statusStr = "da_dang";
        Switch switchStatus = findViewById(R.id.switch_status);
        if (switchStatus != null && switchStatus.isChecked()) {
            statusStr = "hoan_thanh";
        }

        RequestBody title = RequestBody.create(MediaType.parse("text/plain"), bookTitle);
        RequestBody desc = RequestBody.create(MediaType.parse("text/plain"), bookDescription);
        RequestBody status = RequestBody.create(MediaType.parse("text/plain"), statusStr);

        Toast.makeText(this, "Đang tạo truyện...", Toast.LENGTH_SHORT).show();

        RetrofitClient.getApi().createStory(token, title, desc, status, new java.util.ArrayList<>(), null)
                .enqueue(new Callback<Story>() {
                    @Override
                    public void onResponse(@NonNull Call<Story> call, @NonNull Response<Story> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            createdStoryId = response.body().getId();
                            Toast.makeText(BookCreateStep2Activity.this, "Đã tạo truyện, hãy thêm chương!", Toast.LENGTH_SHORT).show();
                            navigateToChapterWriter(createdStoryId);
                        } else {
                            Log.e(TAG, "Create story failed: " + response.code());
                            Toast.makeText(BookCreateStep2Activity.this, "Lỗi tạo truyện (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Story> call, @NonNull Throwable t) {
                        Log.e(TAG, "Create story error", t);
                        Toast.makeText(BookCreateStep2Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Mở ChapterWriterActivity với STORY_ID
     */
    private void navigateToChapterWriter(int storyId) {
        Intent intent = new Intent(BookCreateStep2Activity.this, ChapterWriterActivity.class);
        intent.putExtra("STORY_ID", storyId);
        startActivity(intent);
    }
}
