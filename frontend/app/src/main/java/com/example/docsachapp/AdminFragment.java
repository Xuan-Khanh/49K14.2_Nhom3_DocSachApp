package com.example.docsachapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docsachapp.adapter.CollectionAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Collection;
import com.example.docsachapp.model.UserProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFragment extends Fragment {

    private SessionManager sessionManager;
    private TextView tvUsername, tvBio, tvFollowers, tvFollowing, tvStoryCount;
    private RoundedImageView ivAvatar;

    private ActivityResultLauncher<Intent> profileLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        sessionManager = new SessionManager(requireContext());

        // Ánh xạ View cơ bản
        tvUsername = view.findViewById(R.id.tv_username);
        tvBio = view.findViewById(R.id.tv_bio);
        tvFollowers = view.findViewById(R.id.tv_followers);
        tvFollowing = view.findViewById(R.id.tv_following);
        tvStoryCount = view.findViewById(R.id.tv_story_count);
        ivAvatar = view.findViewById(R.id.iv_avatar);


        profileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadProfile();
                    }
                });

        setupClickListeners(view);
        loadProfile();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d("PROFILE", "=== onResume called ===");
        loadProfile();
    }

    private void setupClickListeners(View view) {
        View rlUserInfo = view.findViewById(R.id.rl_user_info);
        View rlLibrary = view.findViewById(R.id.rl_library);
        Button btnLogout = view.findViewById(R.id.btn_logout);
        View llStoryCount = view.findViewById(R.id.ll_story_count);
        View llFollowers = view.findViewById(R.id.ll_followers);
        View llFollowing = view.findViewById(R.id.ll_following);

        if (rlUserInfo != null) {
            rlUserInfo.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ProfileDetailsActivity.class);
                profileLauncher.launch(intent);
            });
        }

        if (rlLibrary != null) {
            rlLibrary.setOnClickListener(v -> switchTab(R.id.nav_list));
        }

        if (llStoryCount != null) {
            llStoryCount.setOnClickListener(v -> switchTab(R.id.nav_write));
        }

        if (llFollowers != null) {
            llFollowers.setOnClickListener(v -> replaceFragment(new FollowersFragment()));
        }

        if (llFollowing != null) {
            llFollowing.setOnClickListener(v -> replaceFragment(new FollowingFragment()));
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
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

    private void switchTab(int navId) {
        if (getActivity() instanceof MainActivity) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(navId);
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void loadProfile() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;

        RetrofitClient.getApi().getUserProfile(token).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    displayProfile(response.body());
                }
            }
            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Không thể tải thông tin cá nhân", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displayProfile(UserProfile profile) {
        if (tvUsername != null) tvUsername.setText(profile.getUsername());
        if (tvBio != null) tvBio.setText(profile.getBio() == null || profile.getBio().isEmpty() ? "Chưa có mô tả" : profile.getBio());

        // CẬP NHẬT CÁC CON SỐ THỰC TẾ TỪ API
        if (tvFollowers != null) tvFollowers.setText(String.valueOf(profile.getFollowerCount()));
        if (tvFollowing != null) tvFollowing.setText(String.valueOf(profile.getFollowingCount()));
        if (tvStoryCount != null) tvStoryCount.setText(String.valueOf(profile.getStoryCount()));

        if (ivAvatar != null && profile.getAvatar() != null) {
            Glide.with(this).load(profile.getAvatar())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .circleCrop().into(ivAvatar);
        }
    }


}
