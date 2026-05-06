package com.example.docsachapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.docsachapp.model.UserSearchItem;
import com.makeramen.roundedimageview.RoundedImageView;
import java.util.List;

// Adapter dùng để hiển thị danh sách User (trong kết quả Tìm kiếm)
public class UserFollowAdapter extends RecyclerView.Adapter<UserFollowAdapter.ViewHolder> {

    private List<UserSearchItem> list;

    public UserFollowAdapter(List<UserSearchItem> list) {
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
        UserSearchItem item = list.get(position);
        holder.tvUsername.setText(item.getUsername());
        
        String avatarUrl = item.getAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            // Đảm bảo URL là tuyệt đối. Nếu backend Django trả về path tương đối (vd: /media/avatars/...), 
            // cần nối thêm BASE_URL (http://10.0.2.2:8000) vào trước để Glide tải được ảnh.
            String fullUrl = avatarUrl.startsWith("http") ? avatarUrl : "http://10.0.2.2:8000" + avatarUrl;
            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

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
