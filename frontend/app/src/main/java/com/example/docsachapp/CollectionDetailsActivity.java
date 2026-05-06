package com.example.docsachapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.adapter.StoryManageAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Collection;
import com.example.docsachapp.model.Story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CollectionDetailsActivity extends AppCompatActivity {

    private int collectionId;
    private RecyclerView rvStories;
    private StoryManageAdapter adapter;
    private List<Story> storyList = new ArrayList<>();
    
    private TextView tvName, tvCount;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private ImageView btnMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_details); // Nạp giao diện XML

        // Lấy ID của Bộ sưu tập được truyền từ màn hình trước. Nếu không có (lỗi) thì đóng màn hình.
        collectionId = getIntent().getIntExtra("COLLECTION_ID", -1);
        if (collectionId == -1) {
            finish();
            return;
        }

        sessionManager = new SessionManager(this); // Lấy công cụ lấy Token
        initViews(); // Gọi hàm cài đặt giao diện
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi khi màn hình này hiển thị lại (VD: từ màn "Thêm truyện" quay về), tải lại dữ liệu mới nhất
        loadCollectionDetails();
    }

    private void initViews() {
        // Ánh xạ các UI từ XML
        tvName = findViewById(R.id.tv_collection_name);
        tvCount = findViewById(R.id.tv_book_count);
        rvStories = findViewById(R.id.rv_stories);
        progressBar = findViewById(R.id.progress_bar);
        btnMore = findViewById(R.id.btn_more);
        
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish()); // Bấm Back thì tắt màn hình

        // Cài đặt nút Menu 3 chấm
        btnMore.setOnClickListener(v -> {
             PopupMenu popup = new PopupMenu(this, btnMore);
             popup.getMenu().add("Thêm truyện");
             popup.getMenu().add("Chỉnh sửa bộ sưu tập");
             popup.getMenu().add("Xóa bộ sưu tập");
             
             popup.setOnMenuItemClickListener(item -> {
                 String title = item.getTitle().toString();
                 if (title.equals("Xóa bộ sưu tập")) {
                     showDeleteCollectionDialog(); // Mở popup xóa BST
                 } else if (title.equals("Chỉnh sửa bộ sưu tập")) {
                     showEditCollectionDialog(); // Mở popup đổi tên BST
                 } else if (title.equals("Thêm truyện")) {
                     // Chuyển sang màn hình chọn truyện để thêm, gửi kèm theo ID của BST hiện tại
                     Intent intent = new Intent(this, AddStoriesToCollectionActivity.class);
                     intent.putExtra("COLLECTION_ID", collectionId);
                     startActivity(intent);
                 }
                 return true;
             });
             popup.show();
        });

        // Khởi tạo Adapter với Story object để xóa chính xác
        // Truyền một Callback (sự kiện) vào Adapter: Khi người dùng bấm xóa 1 cuốn truyện, nó sẽ gọi showRemoveStoryDialog
        adapter = new StoryManageAdapter(storyList, this, story -> {
            showRemoveStoryDialog(story);
        });
        
        rvStories.setLayoutManager(new LinearLayoutManager(this));
        rvStories.setAdapter(adapter);
    }

    // Lấy thông tin chi tiết của Bộ sưu tập từ Server
    private void loadCollectionDetails() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        String token = sessionManager.getAuthHeader();

        RetrofitClient.getApi().getCollectionDetail(token, collectionId).enqueue(new Callback<Collection>() {
            @Override
            public void onResponse(Call<Collection> call, Response<Collection> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Collection collection = response.body();
                    
                    // Cập nhật giao diện: Tên và Số lượng truyện
                    tvName.setText(collection.getName());
                    tvCount.setText(collection.getStoryCount() + " truyện");
                    
                    // Xóa danh sách cũ, đổ danh sách truyện mới vào để hiển thị
                    storyList.clear();
                    if (collection.getStories() != null) {
                        storyList.addAll(collection.getStories());
                    }
                    adapter.notifyDataSetChanged(); // Vẽ lại danh sách
                }
            }
            @Override public void onFailure(Call<Collection> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Bật hộp thoại hỏi xác nhận có chắc muốn xóa 1 truyện cụ thể khỏi Bộ sưu tập không
    private void showRemoveStoryDialog(Story story) {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_remove_story, null);

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            removeStoryApi(story, dialog); // Bấm Đồng ý thì tiến hành gọi API xóa
        });

        dialog.setView(view);
        dialog.show();
    }

    // Gửi API loại bỏ 1 truyện ra khỏi Bộ sưu tập
    private void removeStoryApi(Story story, AlertDialog dialog) {
        String token = sessionManager.getAuthHeader();
        Map<String, Object> body = new HashMap<>();
        body.put("collection_id", collectionId); // Xóa khỏi BST nào?
        body.put("story_id", story.getId());     // Xóa cuốn truyện nào?

        RetrofitClient.getApi().removeStoryFromCollection(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    // Xóa item chính xác bằng cách tìm đối tượng trong list thay vì dùng index
                    // (Làm cách này để không phải tốn thời gian/data mạng gọi API load lại toàn bộ danh sách)
                    storyList.remove(story); 
                    adapter.notifyDataSetChanged(); // Báo adapter xóa khung của truyện này đi
                    tvCount.setText(storyList.size() + " truyện"); // Cập nhật lại tổng số truyện trên màn hình
                    Toast.makeText(CollectionDetailsActivity.this, "Đã xóa khỏi bộ sưu tập", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(CollectionDetailsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    // Bật hộp thoại xóa VĨNH VIỄN toàn bộ Bộ sưu tập này
    private void showDeleteCollectionDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_collection, null);

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            RetrofitClient.getApi().deleteCollection(sessionManager.getAuthHeader(), collectionId).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(CollectionDetailsActivity.this, "Đã xóa bộ sưu tập", Toast.LENGTH_SHORT).show();
                        finish(); // Xóa xong thì tắt luôn màn hình này, tự quay về Thư Viện
                    }
                    dialog.dismiss();
                }
                @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) { dialog.dismiss(); }
            });
        });
        dialog.setView(view);
        dialog.show();
    }

    // Bật hộp thoại Đổi tên Bộ sưu tập
    private void showEditCollectionDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_collection, null);
        
        android.widget.EditText etName = view.findViewById(R.id.et_collection_name);
        etName.setText(tvName.getText().toString()); // Tự động điền sẵn tên hiện tại vào ô nhập

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            if (newName.isEmpty()) return; // Không cho nhập rỗng

            Map<String, Object> body = new HashMap<>();
            body.put("ten_bo_suu_tap", newName);

            // Gửi API cập nhật tên mới
            RetrofitClient.getApi().updateCollection(sessionManager.getAuthHeader(), collectionId, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        tvName.setText(newName); // Sửa thành công thì cập nhật ngay chữ trên màn hình
                        Toast.makeText(CollectionDetailsActivity.this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
                @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) { dialog.dismiss(); }
            });
        });
        dialog.setView(view);
        dialog.show();
    }
}