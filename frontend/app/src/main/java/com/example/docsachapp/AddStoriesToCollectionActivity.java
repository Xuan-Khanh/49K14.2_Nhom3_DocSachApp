package com.example.docsachapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.adapter.StoryGridAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Collection;
import com.example.docsachapp.model.Story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddStoriesToCollectionActivity extends AppCompatActivity {

    private int collectionId;
    private RecyclerView rvStories;
    private StoryGridAdapter adapter;
    private List<Story> storyList = new ArrayList<>();
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stories_to_collection); // Nạp giao diện XML

        // Lấy ID của bộ sưu tập được truyền từ màn hình trước sang. Mặc định là -1 nếu lỗi.
        collectionId = getIntent().getIntExtra("COLLECTION_ID", -1);
        if (collectionId == -1) {
            finish(); // Nếu không có ID hợp lệ, đóng màn hình luôn
            return;
        }

        sessionManager = new SessionManager(this); // Khởi tạo bộ quản lý phiên (lấy token)
        initViews(); // Ánh xạ và cài đặt giao diện
        loadCollectionAndStories(); // Tải dữ liệu từ mạng
    }

    private void initViews() {
        // Ánh xạ các thành phần giao diện
        rvStories = findViewById(R.id.rv_add_stories);
        progressBar = findViewById(R.id.progress_bar);
        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView btnDone = findViewById(R.id.btn_done);

        btnBack.setOnClickListener(v -> finish()); // Bấm nút Back -> Tắt màn hình

        // Cài đặt Adapter cho danh sách truyện
        adapter = new StoryGridAdapter(storyList, this);
        adapter.setSelectionMode(true); // Bật chế độ tick chọn truyện (Selection Mode)
        rvStories.setLayoutManager(new GridLayoutManager(this, 3)); // Chia lưới 3 cột
        rvStories.setAdapter(adapter);

        // Xử lý sự kiện khi bấm nút Xong (dấu check)
        btnDone.setOnClickListener(v -> {
            Set<Integer> selectedIds = adapter.getSelectedStoryIds(); // Lấy các ID đã được tick
            if (selectedIds.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 truyện mới", Toast.LENGTH_SHORT).show();
            } else {
                addStoriesToCollection(selectedIds); // Gọi hàm thêm vào DB
            }
        });
    }

    // Lấy thông tin BST xem đã có những truyện nào rồi (để không cho tick lại), sau đó tải danh sách truyện
    private void loadCollectionAndStories() {
        progressBar.setVisibility(View.VISIBLE); // Hiện vòng xoay tải dữ liệu
        String token = sessionManager.getAuthHeader();

        // 1. Lấy chi tiết bộ sưu tập để biết truyện nào đã có sẵn
        RetrofitClient.getApi().getCollectionDetail(token, collectionId).enqueue(new Callback<Collection>() {
            @Override
            public void onResponse(Call<Collection> call, Response<Collection> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Set<Integer> existingIds = new HashSet<>(); // Giỏ chứa ID truyện ĐÃ CÓ trong BST
                    if (response.body().getStories() != null) {
                        for (Story s : response.body().getStories()) {
                            existingIds.add(s.getId());
                        }
                    }
                    // Báo cho Adapter biết những ID này đã tồn tại (Adapter sẽ làm mờ/khóa ô tick)
                    adapter.setAlreadyAddedIds(existingIds);
                }
                // 2. Sau khi kiểm tra xong, bắt đầu tải toàn bộ danh sách truyện đang theo dõi
                loadFollowingStories();
            }

            @Override
            public void onFailure(Call<Collection> call, Throwable t) {
                loadFollowingStories(); // Kể cả lỗi cũng ráng tải danh sách truyện cho user xem
            }
        });
    }

    // Gọi API lấy danh sách toàn bộ truyện user đang theo dõi
    private void loadFollowingStories() {
        String token = sessionManager.getAuthHeader();
        RetrofitClient.getApi().getFollowingStories(token).enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                progressBar.setVisibility(View.GONE); // Tải xong thì tắt vòng xoay
                if (response.isSuccessful() && response.body() != null) {
                    storyList.clear(); // Xóa dữ liệu cũ
                    storyList.addAll(response.body()); // Đổ dữ liệu mới từ Server vào
                    adapter.notifyDataSetChanged(); // Yêu cầu Adapter vẽ lại giao diện
                }
            }

            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddStoriesToCollectionActivity.this, "Lỗi tải danh sách truyện", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Xử lý gửi API nạp từng truyện đã tick vào Bộ sưu tập
    private void addStoriesToCollection(Set<Integer> storyIds) {
        String token = sessionManager.getAuthHeader();
        int total = storyIds.size(); // Tổng số truyện cần thêm
        final int[] count = {0}; // Biến đếm số lượng API đã chạy xong

        // Lặp qua từng ID truyện đã tick để gọi API
        for (Integer sId : storyIds) {
            Map<String, Object> body = new HashMap<>(); // Đóng gói dữ liệu gửi đi
            body.put("collection_id", collectionId);
            body.put("story_id", sId);

            RetrofitClient.getApi().addStoryToCollection(token, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    count[0]++; // Đếm tiến độ (API này chạy xong thì tăng lên 1)
                    
                    // Nếu số API chạy xong BẰNG tổng số cần chạy -> Hoàn tất
                    if (count[0] == total) {
                        Toast.makeText(AddStoriesToCollectionActivity.this, "Đã thêm thành công", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng màn hình hiện tại
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    // (Để trống) Nếu lỗi kết nối 1 truyện thì bỏ qua, chạy tiếp truyện khác
                }
            });
        }
    }
}