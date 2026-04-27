package com.example.docsachapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docsachapp.AuthorProfileActivity;
import com.example.docsachapp.R;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.UserSearchItem;
import com.google.android.material.button.MaterialButton;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowListAdapter extends RecyclerView.Adapter<FollowListAdapter.ViewHolder> {
    private List<UserSearchItem> list;
    private Context context;
    private SessionManager sessionManager;

    public FollowListAdapter(List<UserSearchItem> list, Context context) {
        this.list = list;
        this.context = context;
        this.sessionManager = new SessionManager(context);
    }

    public void updateData(List<UserSearchItem> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_follow_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserSearchItem user = list.get(position);
        holder.tvUsername.setText(user.getUsername());
        
        String bio = user.getMoTa();
        if (bio == null || bio.isEmpty()) {
            holder.tvBio.setVisibility(View.GONE);
        } else {
            holder.tvBio.setVisibility(View.VISIBLE);
            holder.tvBio.setText(bio);
        }

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            String fullUrl = user.getAvatar().startsWith("http") ? user.getAvatar() 
                             : "http://10.0.2.2:8000" + user.getAvatar();
            Glide.with(context).load(fullUrl)
                 .placeholder(R.drawable.image5)
                 .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.image5);
        }

        // ✅ FIX #3: Nếu is_self = true → ẩn nút Follow (không cho follow chính mình)
        if (user.isSelf()) {
            holder.btnFollow.setVisibility(View.GONE);
        } else {
            holder.btnFollow.setVisibility(View.VISIBLE);
            updateFollowButtonUI(holder.btnFollow, user.isFollowing());
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AuthorProfileActivity.class);
            intent.putExtra("USER_ID", user.getId());
            context.startActivity(intent);
        });

        holder.btnFollow.setOnClickListener(v -> {
            toggleFollow(user, holder.btnFollow);
        });
    }

    private void toggleFollow(UserSearchItem user, MaterialButton btnFollow) {
        String authHeader = sessionManager.getAuthHeader();
        if (authHeader == null) {
            Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user.getId() == sessionManager.getUserId()) {
            Toast.makeText(context, "Bạn không thể theo dõi chính mình", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Integer> body = new HashMap<>();
        body.put("following_id", user.getId());

        if (user.isFollowing()) {
            RetrofitClient.getApi().unfollowUser(authHeader, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        user.setFollowing(false);
                        updateFollowButtonUI(btnFollow, false);
                    } else {
                        Toast.makeText(context, "Lỗi bỏ theo dõi", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            RetrofitClient.getApi().followUser(authHeader, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        user.setFollowing(true);
                        updateFollowButtonUI(btnFollow, true);
                    } else {
                        Toast.makeText(context, "Lỗi theo dõi", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateFollowButtonUI(MaterialButton btn, boolean isFollowing) {
        if (isFollowing) {
            btn.setText("Đang theo dõi");
            int primaryTransparent = ContextCompat.getColor(context, R.color.primary_transparent_25);
            int primary = ContextCompat.getColor(context, R.color.primary);
            btn.setBackgroundTintList(ColorStateList.valueOf(primaryTransparent));
            btn.setTextColor(primary);
        } else {
            btn.setText("Theo dõi");
            int primary = ContextCompat.getColor(context, R.color.primary);
            int white = ContextCompat.getColor(context, R.color.white);
            btn.setBackgroundTintList(ColorStateList.valueOf(primary));
            btn.setTextColor(white);
        }
    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView ivAvatar;
        TextView tvUsername, tvBio;
        MaterialButton btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvBio = itemView.findViewById(R.id.tv_bio);
            btnFollow = itemView.findViewById(R.id.btn_follow);
        }
    }
}
