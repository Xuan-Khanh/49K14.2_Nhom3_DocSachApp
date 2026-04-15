package com.example.docsachapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.docsachapp.R;
import com.example.docsachapp.AuthorProfileActivity;
import com.example.docsachapp.model.UserSearchItem;
import com.makeramen.roundedimageview.RoundedImageView;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {
    private List<UserSearchItem> list;
    private Context context;

    public UserSearchAdapter(List<UserSearchItem> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void updateData(List<UserSearchItem> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserSearchItem user = list.get(position);
        holder.tvUsername.setText(user.getUsername());
        holder.tvStoriesCount.setText(user.getSoTruyen() + " tác phẩm");

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            String fullUrl = user.getAvatar().startsWith("http") ? user.getAvatar() 
                             : "http://10.0.2.2:8000" + user.getAvatar();
            Glide.with(context).load(fullUrl)
                 .placeholder(R.drawable.image5)
                 .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.image5);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AuthorProfileActivity.class);
            intent.putExtra("USER_ID", user.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        com.makeramen.roundedimageview.RoundedImageView ivAvatar;
        TextView tvUsername, tvStoriesCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvStoriesCount = itemView.findViewById(R.id.tv_stories_count);
        }
    }
}
