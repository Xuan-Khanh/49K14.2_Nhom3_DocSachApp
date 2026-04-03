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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.adapter.StoryGridAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Story;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryFragment extends Fragment {

    private boolean isFollowingTab = true;
    private RecyclerView rvFollowing;
    private StoryGridAdapter followingAdapter;
    private List<Story> followingStories = new ArrayList<>();
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        sessionManager = new SessionManager(requireContext());

        TextView tabFollowing = view.findViewById(R.id.tab_following);
        TextView tabCollections = view.findViewById(R.id.tab_collections);
        rvFollowing = view.findViewById(R.id.rv_following);
        ScrollView contentCollections = view.findViewById(R.id.content_collections);
        
        LinearLayout topBar = view.findViewById(R.id.ll_topbar);
        RelativeLayout bulkActionBar = view.findViewById(R.id.bulk_action_bar);
        ImageView btnMore = view.findViewById(R.id.btn_more);
        ImageView btnAddCollection = view.findViewById(R.id.btn_add_collection);
        
        ImageView btnCancelBulk = view.findViewById(R.id.btn_cancel_bulk);
        ImageView btnBulkAdd = view.findViewById(R.id.btn_bulk_add);
        ImageView btnBulkDelete = view.findViewById(R.id.btn_bulk_delete);
        
        ImageView btnCollectionMore = view.findViewById(R.id.btn_collection_more);

        // Setup RecyclerView for Following
        followingAdapter = new StoryGridAdapter(followingStories, getContext());
        rvFollowing.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvFollowing.setAdapter(followingAdapter);

        loadFollowingStories();

        // Tab Switching Logic
        View.OnClickListener tabListener = v -> {
            if (v.getId() == R.id.tab_following) {
                isFollowingTab = true;
                tabFollowing.setTextColor(getResources().getColor(R.color.primary));
                view.findViewById(R.id.indicator_following).setVisibility(View.VISIBLE);
                
                tabCollections.setTextColor(getResources().getColor(R.color.placeholder));
                view.findViewById(R.id.indicator_collections).setVisibility(View.INVISIBLE);
                
                rvFollowing.setVisibility(View.VISIBLE);
                contentCollections.setVisibility(View.GONE);
                btnAddCollection.setVisibility(View.GONE);
                btnMore.setVisibility(View.VISIBLE);
            } else {
                isFollowingTab = false;
                tabCollections.setTextColor(getResources().getColor(R.color.primary));
                view.findViewById(R.id.indicator_collections).setVisibility(View.VISIBLE);
                
                tabFollowing.setTextColor(getResources().getColor(R.color.placeholder));
                view.findViewById(R.id.indicator_following).setVisibility(View.INVISIBLE);
                
                contentCollections.setVisibility(View.VISIBLE);
                rvFollowing.setVisibility(View.GONE);
                btnAddCollection.setVisibility(View.VISIBLE);
                btnMore.setVisibility(View.GONE);
            }
        };

        tabFollowing.setOnClickListener(tabListener);
        tabCollections.setOnClickListener(tabListener);

        // 3-dot menu for Following Tab
        btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), btnMore);
            popup.getMenu().add("Sắp xếp theo");
            popup.getMenu().add("Cập nhật truyện");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Sắp xếp theo")) {
                    String[] options = {"Đọc gần đây", "Thêm gần đây", "Cập nhật gần đây"};
                    new AlertDialog.Builder(requireContext())
                        .setItems(options, (dialog, index) -> Toast.makeText(getContext(), options[index], Toast.LENGTH_SHORT).show())
                        .show();
                } else if (item.getTitle().equals("Cập nhật truyện")) {
                    topBar.setVisibility(View.GONE);
                    bulkActionBar.setVisibility(View.VISIBLE);
                }
                return true;
            });
            popup.show();
        });

        // Bulk Actions
        btnCancelBulk.setOnClickListener(v -> {
            bulkActionBar.setVisibility(View.GONE);
            topBar.setVisibility(View.VISIBLE);
        });
        
        btnBulkDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setMessage("Bạn có muốn xóa Truyện này?")
                .setPositiveButton("Xóa", (d, w) -> Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Hủy", null)
                .show();
        });

        return view;
    }

    private void loadFollowingStories() {
        // FIX: Dùng getAuthHeader() thay vì tự ghép "Token "
        String token = sessionManager.getAuthHeader();
        if (token == null) return;

        RetrofitClient.getApi().getFollowingStories(token).enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    followingStories.clear();
                    followingStories.addAll(response.body());
                    followingAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi tải danh sách theo dõi", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}