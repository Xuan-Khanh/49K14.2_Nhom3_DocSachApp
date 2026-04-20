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
        setContentView(R.layout.activity_collection_details);

        collectionId = getIntent().getIntExtra("COLLECTION_ID", -1);
        if (collectionId == -1) {
            finish();
            return;
        }

        sessionManager = new SessionManager(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCollectionDetails();
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_collection_name);
        tvCount = findViewById(R.id.tv_book_count);
        rvStories = findViewById(R.id.rv_stories);
        progressBar = findViewById(R.id.progress_bar);
        btnMore = findViewById(R.id.btn_more);
        
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        btnMore.setOnClickListener(v -> {
             PopupMenu popup = new PopupMenu(this, btnMore);
             popup.getMenu().add("Thêm truyện");
             popup.getMenu().add("Chỉnh sửa bộ sưu tập");
             popup.getMenu().add("Xóa bộ sưu tập");
             
             popup.setOnMenuItemClickListener(item -> {
                 String title = item.getTitle().toString();
                 if (title.equals("Xóa bộ sưu tập")) {
                     showDeleteCollectionDialog();
                 } else if (title.equals("Chỉnh sửa bộ sưu tập")) {
                     showEditCollectionDialog();
                 } else if (title.equals("Thêm truyện")) {
                     Intent intent = new Intent(this, AddStoriesToCollectionActivity.class);
                     intent.putExtra("COLLECTION_ID", collectionId);
                     startActivity(intent);
                 }
                 return true;
             });
             popup.show();
        });

        // Khởi tạo Adapter với Story object để xóa chính xác
        adapter = new StoryManageAdapter(storyList, this, story -> {
            showRemoveStoryDialog(story);
        });
        
        rvStories.setLayoutManager(new LinearLayoutManager(this));
        rvStories.setAdapter(adapter);
    }

    private void loadCollectionDetails() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        String token = sessionManager.getAuthHeader();

        RetrofitClient.getApi().getCollectionDetail(token, collectionId).enqueue(new Callback<Collection>() {
            @Override
            public void onResponse(Call<Collection> call, Response<Collection> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Collection collection = response.body();
                    tvName.setText(collection.getName());
                    tvCount.setText(collection.getStoryCount() + " truyện");
                    
                    storyList.clear();
                    if (collection.getStories() != null) {
                        storyList.addAll(collection.getStories());
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<Collection> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showRemoveStoryDialog(Story story) {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_remove_story, null);

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            removeStoryApi(story, dialog);
        });

        dialog.setView(view);
        dialog.show();
    }

    private void removeStoryApi(Story story, AlertDialog dialog) {
        String token = sessionManager.getAuthHeader();
        Map<String, Object> body = new HashMap<>();
        body.put("collection_id", collectionId);
        body.put("story_id", story.getId());

        RetrofitClient.getApi().removeStoryFromCollection(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    // Xóa item chính xác bằng cách tìm đối tượng trong list thay vì dùng index
                    storyList.remove(story);
                    adapter.notifyDataSetChanged();
                    tvCount.setText(storyList.size() + " truyện");
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
                        finish();
                    }
                    dialog.dismiss();
                }
                @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) { dialog.dismiss(); }
            });
        });
        dialog.setView(view);
        dialog.show();
    }

    private void showEditCollectionDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_collection, null);
        android.widget.EditText etName = view.findViewById(R.id.et_collection_name);
        etName.setText(tvName.getText().toString());

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            if (newName.isEmpty()) return;

            Map<String, Object> body = new HashMap<>();
            body.put("ten_bo_suu_tap", newName);

            RetrofitClient.getApi().updateCollection(sessionManager.getAuthHeader(), collectionId, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        tvName.setText(newName);
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