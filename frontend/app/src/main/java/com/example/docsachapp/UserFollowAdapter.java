package com.example.docsachapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.docsachapp.model.UserFollowItem;
import com.makeramen.roundedimageview.RoundedImageView;
import java.util.List;

public class UserFollowAdapter extends RecyclerView.Adapter<UserFollowAdapter.ViewHolder> {

    private List<UserFollowItem> list;

    public UserFollowAdapter(List<UserFollowItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_follow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserFollowItem item = list.get(position);
        holder.tvUsername.setText(item.getUsername());
        if (item.getAvatar() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getAvatar())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .circleCrop()
                    .into(holder.ivAvatar);
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView ivAvatar;
        TextView tvUsername;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
        }
    }
}