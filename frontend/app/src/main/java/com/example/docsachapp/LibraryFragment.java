package com.example.docsachapp;

import android.app.AlertDialog;
import android.content.Intent;
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

public class LibraryFragment extends Fragment {

    private boolean isFollowingTab = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        TextView tabFollowing = view.findViewById(R.id.tab_following);
        TextView tabCollections = view.findViewById(R.id.tab_collections);
        ScrollView contentFollowing = view.findViewById(R.id.content_following);
        ScrollView contentCollections = view.findViewById(R.id.content_collections);
        
        LinearLayout topBar = view.findViewById(R.id.ll_topbar);
        RelativeLayout bulkActionBar = view.findViewById(R.id.bulk_action_bar);
        ImageView btnMore = view.findViewById(R.id.btn_more);
        ImageView btnAddCollection = view.findViewById(R.id.btn_add_collection);
        
        ImageView btnCancelBulk = view.findViewById(R.id.btn_cancel_bulk);
        ImageView btnBulkAdd = view.findViewById(R.id.btn_bulk_add);
        ImageView btnBulkDelete = view.findViewById(R.id.btn_bulk_delete);
        
        ImageView btnCollectionMore = view.findViewById(R.id.btn_collection_more);

        // Book Item in Following Tab
        View bookItem1 = view.findViewById(R.id.book_item_1);
        if (bookItem1 != null) {
            bookItem1.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), BookDetailsActivity.class);
                startActivity(intent);
            });
        }

        // Collection Item in Collections Tab
        View collectionItem = view.findViewById(R.id.layout_collection_item);
        if (collectionItem != null) {
            collectionItem.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), CollectionDetailsActivity.class);
                startActivity(intent);
            });
        }

        // Tab Switching Logic
        View.OnClickListener tabListener = v -> {
            if (v.getId() == R.id.tab_following) {
                isFollowingTab = true;
                tabFollowing.setTextColor(getResources().getColor(R.color.primary));
                view.findViewById(R.id.indicator_following).setVisibility(View.VISIBLE);
                
                tabCollections.setTextColor(getResources().getColor(R.color.placeholder));
                view.findViewById(R.id.indicator_collections).setVisibility(View.INVISIBLE);
                
                contentFollowing.setVisibility(View.VISIBLE);
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
                contentFollowing.setVisibility(View.GONE);
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
                        .setItems(options, (dialog, which) -> Toast.makeText(getContext(), options[which], Toast.LENGTH_SHORT).show())
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
                .setMessage("Bạn có muốn xóa Truyện này?Xóa 01 truyện?")
                .setPositiveButton("Xóa", (d, w) -> Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Hủy", null)
                .show();
        });

        btnBulkAdd.setOnClickListener(v -> {
             String[] collections = {"Tinh Hoa Kinh Điển", "Truyện Ngắn"};
             new AlertDialog.Builder(requireContext())
                 .setTitle("Thêm vào Bộ sưu tập")
                 .setItems(collections, (dialog, which) -> Toast.makeText(getContext(), "Đã thêm", Toast.LENGTH_SHORT).show())
                 .show();
        });

        // Collections Tab + button
        btnAddCollection.setOnClickListener(v -> {
            EditText input = new EditText(requireContext());
            input.setHint("Tên Bộ sưu tập");
            new AlertDialog.Builder(requireContext())
                .setTitle("Tạo Bộ sưu tập mới")
                .setView(input)
                .setPositiveButton("Tạo", (d, w) -> Toast.makeText(getContext(), "Đã tạo: " + input.getText(), Toast.LENGTH_SHORT).show())
                .setNegativeButton("Hủy", null)
                .show();
        });

        // Collection Item More Button
        if (btnCollectionMore != null) {
            btnCollectionMore.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(requireContext(), btnCollectionMore);
                popup.getMenu().add("Thêm truyện");
                popup.getMenu().add("Đổi tên bộ sưu tập");
                popup.getMenu().add("Xóa bộ sưu tập");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals("Xóa bộ sưu tập")) {
                        new AlertDialog.Builder(requireContext())
                            .setMessage("Bạn có muốn xóa BST này? Toàn bộ truyện trong bộ sưu tập này sẽ bị xóa.")
                            .setPositiveButton("Xóa", null)
                            .setNegativeButton("Hủy", null)
                            .show();
                    } else {
                        Toast.makeText(getContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
                popup.show();
            });
        }

        return view;
    }
}
