package com.example.docsachapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docsachapp.AddStoriesToCollectionActivity;
import com.example.docsachapp.CollectionDetailsActivity;
import com.example.docsachapp.R;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Collection;
import com.example.docsachapp.model.Story;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private List<Collection> collectionList;
    private Context context;
    private SessionManager sessionManager;

    public CollectionAdapter(List<Collection> collectionList, Context context) {
        this.collectionList = collectionList;
        this.context = context;
        this.sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection collection = collectionList.get(position);
        holder.tvName.setText(collection.getName());
        holder.tvCount.setText(collection.getStoryCount() + " truyện");

        List<Story> stories = collection.getStories();
        if (stories != null && !stories.isEmpty()) {
            Glide.with(context).load(stories.get(0).getCoverUrl()).placeholder(R.drawable.image5).into(holder.ivMain);
            if (stories.size() > 1) Glide.with(context).load(stories.get(1).getCoverUrl()).placeholder(R.drawable.image5).into(holder.ivSub1);
            if (stories.size() > 2) Glide.with(context).load(stories.get(2).getCoverUrl()).placeholder(R.drawable.image5).into(holder.ivSub2);
        } else {
            holder.ivMain.setImageResource(R.drawable.image5);
            holder.ivSub1.setImageResource(R.drawable.image5);
            holder.ivSub2.setImageResource(R.drawable.image5);
        }

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMore);
            popup.getMenu().add("Thêm truyện");
            popup.getMenu().add("Chỉnh sửa bộ sưu tập");
            popup.getMenu().add("Xóa bộ sưu tập");
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Thêm truyện")) {
                    Intent intent = new Intent(context, AddStoriesToCollectionActivity.class);
                    intent.putExtra("COLLECTION_ID", collection.getId());
                    context.startActivity(intent);
                } else if (title.equals("Chỉnh sửa bộ sưu tập")) {
                    showEditDialog(collection, position);
                } else if (title.equals("Xóa bộ sưu tập")) {
                    showDeleteDialog(collection, position);
                }
                return true;
            });
            popup.show();
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CollectionDetailsActivity.class);
            intent.putExtra("COLLECTION_ID", collection.getId());
            context.startActivity(intent);
        });
    }

    private void showEditDialog(Collection collection, int position) {
        AlertDialog dialog = new AlertDialog.Builder(context, R.style.CustomAlertDialog).create();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_collection, null);
        EditText etName = view.findViewById(R.id.et_collection_name);
        etName.setText(collection.getName());
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            if (newName.isEmpty()) return;
            Map<String, Object> body = new HashMap<>();
            body.put("ten_bo_suu_tap", newName);
            // Cập nhật lại logic gọi API cho khớp với ApiService mới (nếu có)
            Toast.makeText(context, "Tính năng đang cập nhật để khớp với API mới", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.setView(view); dialog.show();
    }

    private void showDeleteDialog(Collection collection, int position) {
        AlertDialog dialog = new AlertDialog.Builder(context, R.style.CustomAlertDialog).create();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete_collection, null);
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            Toast.makeText(context, "Tính năng đang cập nhật để khớp với API mới", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.setView(view); dialog.show();
    }

    @Override
    public int getItemCount() { return collectionList != null ? collectionList.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView ivMain, ivSub1, ivSub2;
        TextView tvName, tvCount;
        ImageView btnMore;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMain = itemView.findViewById(R.id.iv_cover_main);
            ivSub1 = itemView.findViewById(R.id.iv_cover_sub1);
            ivSub2 = itemView.findViewById(R.id.iv_cover_sub2);
            tvName = itemView.findViewById(R.id.tv_collection_name);
            tvCount = itemView.findViewById(R.id.tv_story_count);
            btnMore = itemView.findViewById(R.id.btn_collection_more);
        }
    }
}
