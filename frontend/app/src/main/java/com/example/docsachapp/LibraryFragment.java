package com.example.docsachapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.adapter.CollectionAdapter;
import com.example.docsachapp.adapter.StoryGridAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Collection;
import com.example.docsachapp.model.Story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryFragment extends Fragment {

    private RecyclerView rvFollowing, rvCollections;
    private StoryGridAdapter followingAdapter;
    private CollectionAdapter collectionAdapter;
    
    private List<Story> followingStories = new ArrayList<>();
    private List<Collection> collections = new ArrayList<>();
    
    private SessionManager sessionManager;
    private LinearLayout llTopBar;
    private RelativeLayout bulkActionBar;
    private TextView tvSelectionCount, tabFollowing, tabCollections;
    private ImageView btnAddCollection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        sessionManager = new SessionManager(requireContext());

        // View mapping
        llTopBar = view.findViewById(R.id.ll_topbar);
        bulkActionBar = view.findViewById(R.id.bulk_action_bar);
        tvSelectionCount = view.findViewById(R.id.tv_selection_count);
        btnAddCollection = view.findViewById(R.id.btn_add_collection);
        
        tabFollowing = view.findViewById(R.id.tab_following);
        tabCollections = view.findViewById(R.id.tab_collections);
        
        rvFollowing = view.findViewById(R.id.rv_following);
        rvCollections = view.findViewById(R.id.rv_collections);

        ImageView btnMore = view.findViewById(R.id.btn_more);
        ImageView btnCancelBulk = view.findViewById(R.id.btn_cancel_bulk);
        ImageView btnBulkDelete = view.findViewById(R.id.btn_bulk_delete);

        setupRecyclerViews();
        setupTabListeners();

        // ─── Menu 3 chấm ──────────────────────────────────────────────────────
        btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), btnMore);
            popup.getMenu().add("Sắp xếp truyện");
            popup.getMenu().add("Cập nhật thư viện");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Sắp xếp truyện")) {
                    showSortDialog();
                } else if (item.getTitle().equals("Cập nhật thư viện")) {
                    enterSelectionMode();
                }
                return true;
            });
            popup.show();
        });

        // ─── Nút Thêm Bộ Sưu Tập ─────────────────────────────────────────────
        btnAddCollection.setOnClickListener(v -> showCreateCollectionDialog());

        // ─── Thoát chế độ chọn ────────────────────────────────────────────────
        btnCancelBulk.setOnClickListener(v -> exitSelectionMode());

        // ─── Xóa hàng loạt ────────────────────────────────────────────────────
        btnBulkDelete.setOnClickListener(v -> {
            Set<Integer> selectedIds = followingAdapter.getSelectedStoryIds();
            if (!selectedIds.isEmpty()) {
                showDeleteConfirmDialog(selectedIds.size());
            }
        });

        loadFollowingStories();
        return view;
    }

    private void setupRecyclerViews() {
        followingAdapter = new StoryGridAdapter(followingStories, getContext());
        rvFollowing.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvFollowing.setAdapter(followingAdapter);

        followingAdapter.setOnSelectionChangeListener(count -> {
            if (tvSelectionCount != null) tvSelectionCount.setText("Đã chọn " + count + " truyện");
        });

        collectionAdapter = new CollectionAdapter(collections, getContext());
        rvCollections.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCollections.setAdapter(collectionAdapter);
    }

    private void setupTabListeners() {
        tabFollowing.setOnClickListener(v -> switchTab(true));
        tabCollections.setOnClickListener(v -> switchTab(false));
    }

    private void switchTab(boolean isFollowing) {
        tabFollowing.setTextColor(getResources().getColor(isFollowing ? R.color.primary : R.color.placeholder));
        tabCollections.setTextColor(getResources().getColor(isFollowing ? R.color.placeholder : R.color.primary));
        
        rvFollowing.setVisibility(isFollowing ? View.VISIBLE : View.GONE);
        rvCollections.setVisibility(isFollowing ? View.GONE : View.VISIBLE);
        btnAddCollection.setVisibility(isFollowing ? View.GONE : View.VISIBLE);
        
        exitSelectionMode();
        if (isFollowing) loadFollowingStories(); else loadCollections();
    }

    // ─── Popup Xóa Truyện ─────────────────────────────────────────────────────
    private void showDeleteConfirmDialog(int count) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_confirm_delete, null);
        
        TextView tvSubMessage = view.findViewById(R.id.tv_sub_message);
        tvSubMessage.setText("Xóa " + (count < 10 ? "0" + count : count) + " truyện?");
        
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            // TODO: Call API unfollow
            Toast.makeText(getContext(), "Đã xóa thành công", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            exitSelectionMode();
            loadFollowingStories();
        });

        dialog.setView(view);
        dialog.show();
    }

    // ─── Popup Tạo Bộ Sưu Tập ────────────────────────────────────────────────
    private void showCreateCollectionDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_create_collection, null);
        
        EditText etName = view.findViewById(R.id.et_collection_name);
        
        view.findViewById(R.id.btn_skip).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_write).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                createCollectionApi(name, dialog);
            }
        });

        dialog.setView(view);
        dialog.show();
    }

    private void createCollectionApi(String name, AlertDialog dialog) {
        String token = sessionManager.getAuthHeader();
        Map<String, Object> body = new HashMap<>();
        body.put("ten_bo_suu_tap", name);

        RetrofitClient.getApi().createCollection(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã tạo bộ sưu tập", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadCollections();
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private void showSortDialog() {
        String[] options = {"Mới cập nhật", "Tên truyện (A-Z)", "Đọc gần đây"};
        new AlertDialog.Builder(requireContext()).setTitle("Sắp xếp theo")
                .setItems(options, (d, i) -> Toast.makeText(getContext(), options[i], 0).show()).show();
    }

    private void enterSelectionMode() {
        llTopBar.setVisibility(View.GONE);
        bulkActionBar.setVisibility(View.VISIBLE);
        followingAdapter.setSelectionMode(true);
    }

    private void exitSelectionMode() {
        bulkActionBar.setVisibility(View.GONE);
        llTopBar.setVisibility(View.VISIBLE);
        followingAdapter.setSelectionMode(false);
    }

    private void loadFollowingStories() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        RetrofitClient.getApi().getFollowingStories(token).enqueue(new Callback<List<Story>>() {
            @Override public void onResponse(Call<List<Story>> c, Response<List<Story>> r) {
                if (isAdded() && r.isSuccessful() && r.body() != null) {
                    followingStories.clear(); followingStories.addAll(r.body());
                    followingAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<List<Story>> c, Throwable t) {}
        });
    }

    private void loadCollections() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;

        // FIX: Gọi getBoSuuTap và nhận kết quả là List<Collection> trực tiếp
        RetrofitClient.getApi().getBoSuuTap(token).enqueue(new Callback<List<Collection>>() {
            @Override
            public void onResponse(Call<List<Collection>> call, Response<List<Collection>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    collections.clear();
                    collections.addAll(response.body());
                    collectionAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Collection>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi tải bộ sưu tập", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
