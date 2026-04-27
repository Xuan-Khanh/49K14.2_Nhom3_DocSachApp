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
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
    private TextView tvSelectionCount;
    private TextView tabFollowing, tabCollections;
    private ImageView btnAddCollection, btnMore, btnBulkAdd, btnBulkDelete;

    private boolean isFollowingTab = true;
    private int selectedCollectionId = -1; // Lưu BST được tích chọn

    /** ✅ FIX #6: Lưu trạng thái sort/filter hiện tại */
    private String currentSortBy = null;
    private String currentOrder = null;
    private String currentTrangThai = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        sessionManager = new SessionManager(requireContext());

        llTopBar = view.findViewById(R.id.ll_topbar);
        bulkActionBar = view.findViewById(R.id.bulk_action_bar);
        tvSelectionCount = view.findViewById(R.id.tv_selection_count);
        btnAddCollection = view.findViewById(R.id.btn_add_collection);
        btnMore = view.findViewById(R.id.btn_more);
        btnBulkAdd = view.findViewById(R.id.btn_bulk_add);
        btnBulkDelete = view.findViewById(R.id.btn_bulk_delete);

        tabFollowing = view.findViewById(R.id.tab_following);
        tabCollections = view.findViewById(R.id.tab_collections);
        
        rvFollowing = view.findViewById(R.id.rv_following);
        rvCollections = view.findViewById(R.id.rv_collections);

        ImageView btnCancelBulk = view.findViewById(R.id.btn_cancel_bulk);

        setupRecyclerViews();
        setupTabListeners();

        // ✅ FIX #3: Đã bỏ mục "Lọc trạng thái" khỏi menu
        btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), btnMore);
            popup.getMenu().add("Sắp xếp truyện");
            popup.getMenu().add("Cập nhật thư viện");
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Sắp xếp truyện")) showSortDialog();
                else if (title.equals("Cập nhật thư viện")) enterSelectionMode();
                return true;
            });
            popup.show();
        });

        btnBulkAdd.setOnClickListener(v -> {
            Set<Integer> selectedIds = followingAdapter.getSelectedStoryIds();
            if (!selectedIds.isEmpty()) {
                showSelectCollectionBottomSheet(selectedIds);
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn truyện trước", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddCollection.setOnClickListener(v -> showCreateCollectionDialog());
        btnCancelBulk.setOnClickListener(v -> exitSelectionMode());
        btnBulkDelete.setOnClickListener(v -> {
            Set<Integer> selectedIds = followingAdapter.getSelectedStoryIds();
            if (!selectedIds.isEmpty()) showDeleteConfirmDialog(selectedIds);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFollowingTab) loadFollowingStories(); else loadCollections();
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
        this.isFollowingTab = isFollowing;
        tabFollowing.setTextColor(getResources().getColor(isFollowing ? R.color.primary : R.color.placeholder));
        tabCollections.setTextColor(getResources().getColor(isFollowing ? R.color.placeholder : R.color.primary));
        rvFollowing.setVisibility(isFollowing ? View.VISIBLE : View.GONE);
        rvCollections.setVisibility(isFollowing ? View.GONE : View.VISIBLE);
        btnAddCollection.setVisibility(isFollowing ? View.GONE : View.VISIBLE);
        btnMore.setVisibility(isFollowing ? View.VISIBLE : View.GONE);
        exitSelectionMode();
        if (isFollowing) loadFollowingStories(); else loadCollections();
    }

    // ✅ FIX #5: Gọi API collection theo user_id, chỉ lấy BST của user hiện tại
    private void loadCollections() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;
        RetrofitClient.getApi().getUserCollections(userId).enqueue(new Callback<List<Collection>>() {
            @Override
            public void onResponse(Call<List<Collection>> call, Response<List<Collection>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    collections.clear();
                    collections.addAll(response.body());
                    collectionAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<List<Collection>> call, Throwable t) {}
        });
    }

    /** ✅ FIX #6: Gọi API với đúng params sort_by, order, trang_thai */
    private void loadFollowingStories() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        RetrofitClient.getApi().getFollowingStories(token, currentSortBy, currentOrder, currentTrangThai, null)
                .enqueue(new Callback<List<Story>>() {
            @Override public void onResponse(Call<List<Story>> c, Response<List<Story>> r) {
                if (isAdded() && r.isSuccessful() && r.body() != null) {
                    followingStories.clear(); followingStories.addAll(r.body());
                    followingAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<List<Story>> c, Throwable t) {}
        });
    }

    private void enterSelectionMode() {
        llTopBar.setVisibility(View.GONE);
        bulkActionBar.setVisibility(View.VISIBLE);
        followingAdapter.setSelectionMode(true);
        loadCollections(); 
    }

    private void exitSelectionMode() {
        bulkActionBar.setVisibility(View.GONE);
        llTopBar.setVisibility(View.VISIBLE);
        followingAdapter.setSelectionMode(false);
    }

    private void showSelectCollectionBottomSheet(Set<Integer> storyIds) {
        selectedCollectionId = -1; // Reset lựa chọn
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_to_collection, null);
        
        RecyclerView rv = dialogView.findViewById(R.id.rv_collections_select);
        rv.setVisibility(View.VISIBLE);
        TextView btnDone = dialogView.findViewById(R.id.tv_done); // Đổi ID thành tv_done cho khớp với XML mới

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.Adapter adapterSelect = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
                View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_collection_select, p, false);
                return new RecyclerView.ViewHolder(v) {};
            }
            @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int p) {
                Collection col = collections.get(p);
                ((TextView)h.itemView.findViewById(R.id.tv_collection_name)).setText(col.getName());
                
                View ivTick = h.itemView.findViewById(R.id.iv_tick);
                ivTick.setVisibility(col.getId() == selectedCollectionId ? View.VISIBLE : View.GONE);

                h.itemView.setOnClickListener(v -> {
                    selectedCollectionId = col.getId();
                    notifyDataSetChanged();
                });
            }
            @Override public int getItemCount() { return collections.size(); }
        };
        rv.setAdapter(adapterSelect);

        btnDone.setOnClickListener(v -> {
            if (selectedCollectionId != -1) {
                addStoriesToCollectionApi(selectedCollectionId, storyIds);
                bottomSheet.dismiss();
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn 1 bộ sưu tập", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheet.setContentView(dialogView);
        bottomSheet.show();
    }

    private void addStoriesToCollectionApi(int collectionId, Set<Integer> storyIds) {
        String token = sessionManager.getAuthHeader();
        for (Integer sId : storyIds) {
            Map<String, Object> body = new HashMap<>();
            body.put("collection_id", collectionId);
            body.put("story_id", sId);
            RetrofitClient.getApi().addStoryToCollection(token, body).enqueue(new Callback<Map<String, Object>>() {
                @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) Toast.makeText(getContext(), "Đã thêm vào bộ sưu tập", Toast.LENGTH_SHORT).show();
                }
                @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
            });
        }
        exitSelectionMode();
    }

    private void showDeleteConfirmDialog(Set<Integer> storyIds) {
        int count = storyIds.size();
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).create();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_delete, null);
        TextView tvSubMessage = dialogView.findViewById(R.id.tv_sub_message);
        tvSubMessage.setText("Xóa " + (count < 10 ? "0" + count : count) + " truyện?");
        
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            unfollowStoriesApi(storyIds, dialog);
        });
        
        dialog.setView(dialogView); 
        dialog.show();
    }

    private void unfollowStoriesApi(Set<Integer> storyIds, AlertDialog dialog) {
        String token = sessionManager.getAuthHeader();
        int total = storyIds.size();
        final int[] successCount = {0};

        for (Integer id : storyIds) {
            Map<String, Object> body = new HashMap<>();
            body.put("story_id", id);
            
            RetrofitClient.getApi().unfollowStory(token, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    successCount[0]++;
                    if (successCount[0] == total) {
                        Toast.makeText(getContext(), "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        exitSelectionMode();
                        loadFollowingStories();
                    }
                }
                @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    dialog.dismiss();
                }
            });
        }
    }

    private void showCreateCollectionDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).create();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_collection, null);
        EditText etName = dialogView.findViewById(R.id.et_collection_name);
        dialogView.findViewById(R.id.btn_skip).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_write).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) createCollectionApi(name, dialog);
        });
        dialog.setView(dialogView); dialog.show();
    }

    private void createCollectionApi(String name, AlertDialog dialog) {
        String token = sessionManager.getAuthHeader();
        Map<String, Object> body = new HashMap<>();
        body.put("ten_bo_suu_tap", name);
        RetrofitClient.getApi().createCollection(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã tạo bộ sưu tập", Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); loadCollections();
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    /**
     * ✅ FIX #6: Dialog sắp xếp gọi API đúng params sort_by và order
     */
    /**
     * ✅ FIX #4: Đã bỏ "Lượt đọc nhiều nhất" và "Mới tạo nhất"
     */
    private void showSortDialog() {
        String[] options = {"Mới cập nhật", "Tên truyện (A-Z)", "Tên truyện (Z-A)"};
        new AlertDialog.Builder(requireContext()).setTitle("Sắp xếp theo")
                .setItems(options, (d, i) -> {
                    switch (i) {
                        case 0: // Mới cập nhật
                            currentSortBy = "updated_at";
                            currentOrder = "desc";
                            break;
                        case 1: // Tên A-Z
                            currentSortBy = "ten_truyen";
                            currentOrder = "asc";
                            break;
                        case 2: // Tên Z-A
                            currentSortBy = "ten_truyen";
                            currentOrder = "desc";
                            break;
                    }
                    loadFollowingStories();
                    Toast.makeText(getContext(), "Đang sắp xếp: " + options[i], Toast.LENGTH_SHORT).show();
                }).show();
    }
}

