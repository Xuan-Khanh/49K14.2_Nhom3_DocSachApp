package com.example.docsachapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.UserProfile;
import com.makeramen.roundedimageview.RoundedImageView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AdminFragment – Màn hình Profile / Cài đặt
 *
 * Fix:
 * 1. Load thông tin profile thật từ GET /api/auth/profile
 * 2. Hiển thị username, bio, follower, avatar
 * 3. Mở ProfileEditActivity và tự refresh khi quay về
 * 4. Đăng xuất xóa session và về LoginActivity
 */
public class AdminFragment extends Fragment {

    private SessionManager sessionManager;
    private TextView tvUsername, tvBio, tvFollowers, tvFollowing, tvStoryCount;
    private RoundedImageView ivAvatar;

    // Launcher để nhận kết quả từ ProfileEditActivity
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        sessionManager = new SessionManager(requireContext());

        // Ánh xạ View
        tvUsername = view.findViewById(R.id.tv_username);
        tvBio = view.findViewById(R.id.tv_bio);
        tvFollowers = view.findViewById(R.id.tv_followers);
        tvFollowing = view.findViewById(R.id.tv_following);
        tvStoryCount = view.findViewById(R.id.tv_story_count);
        ivAvatar = view.findViewById(R.id.iv_avatar);

        // Launcher để detect quay về từ ProfileEditActivity
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Reload profile sau khi edit thành công
                        loadProfile();
                    }
                });

        setupClickListeners(view);
        loadProfile();

        return view;
    }

    // ─── Setup nút bấm ────────────────────────────────────────────────────────
    private void setupClickListeners(View view) {
        View rlUserInfo = view.findViewById(R.id.rl_user_info);
        View rlNotifications = view.findViewById(R.id.rl_notifications);
        View rlLibrary = view.findViewById(R.id.rl_library);
        View rlComments = view.findViewById(R.id.rl_comments);
        View rlLogout = view.findViewById(R.id.rl_logout);

        // Sửa hồ sơ → dùng launcher để refresh sau khi quay về
        if (rlUserInfo != null) {
            rlUserInfo.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ProfileEditActivity.class);
                editProfileLauncher.launch(intent);
            });
        }

        if (rlNotifications != null) {
            rlNotifications.setOnClickListener(v -> Toast
                    .makeText(requireContext(), "Chức năng thông báo đang phát triển", Toast.LENGTH_SHORT).show());
        }

        if (rlLibrary != null) {
            rlLibrary.setOnClickListener(
                    v -> Toast.makeText(requireContext(), "Xem thư viện của tôi", Toast.LENGTH_SHORT).show());
        }

        if (rlComments != null) {
            rlComments.setOnClickListener(
                    v -> Toast.makeText(requireContext(), "Bình luận của tôi", Toast.LENGTH_SHORT).show());
        }

        // Đăng xuất
        if (rlLogout != null) {
            rlLogout.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        sessionManager.logout();
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show());
        }
    }

    // ─── Load profile từ API ──────────────────────────────────────────────────
    private void loadProfile() {
        String token = sessionManager.getAuthHeader();
        if (token == null)
            return;

        // Hiện username từ cache trước khi API về
        if (tvUsername != null) {
            String cached = sessionManager.getUsername();
            if (cached != null)
                tvUsername.setText(cached);
        }

        // Load avatar từ cache nếu có
        if (ivAvatar != null) {
            String cachedAvatar = sessionManager.getAvatar();
            if (cachedAvatar != null) {
                Glide.with(this).load(cachedAvatar)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .circleCrop().into(ivAvatar);
            }
        }

        // Gọi API lấy profile đầy đủ
        RetrofitClient.getApi().getUserProfile(token).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (!isAdded())
                    return;
                if (response.isSuccessful() && response.body() != null) {
                    displayProfile(response.body());
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
            }
        });
    }

    private void displayProfile(UserProfile profile) {
        if (tvUsername != null)
            tvUsername.setText(profile.getUsername());
        if (tvBio != null)
            tvBio.setText(profile.getBio().isEmpty() ? "Chưa có mô tả" : profile.getBio());
        if (tvFollowers != null)
            tvFollowers.setText(String.valueOf(profile.getFollowerCount()));
        if (tvFollowing != null)
            tvFollowing.setText(String.valueOf(profile.getFollowingCount()));
        if (tvStoryCount != null)
            tvStoryCount.setText(String.valueOf(profile.getStoryCount()));

        // Load avatar
        if (ivAvatar != null && profile.getAvatar() != null) {
            sessionManager.saveAvatar(profile.getAvatar());
            Glide.with(this).load(profile.getAvatar())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .circleCrop().into(ivAvatar);
        }
    }
}
