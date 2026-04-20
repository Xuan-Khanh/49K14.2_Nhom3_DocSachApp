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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docsachapp.adapter.CollectionAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Collection;
import com.example.docsachapp.model.UserProfile;
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
    
    private RecyclerView rvCollections;
    private CollectionAdapter collectionAdapter;
    private List<Collection> collectionList = new ArrayList<>();

    private ActivityResultLauncher<Intent> editProfileLauncher;

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
        rvCollections = view.findViewById(R.id.rv_admin_collections);

        // Setup RecyclerView cho Bộ sưu tập
        collectionAdapter = new CollectionAdapter(collectionList, getContext());
        rvCollections.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCollections.setAdapter(collectionAdapter);

        // Launcher để detect quay về từ ProfileEditActivity
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadProfile();
                    }
                });

        setupClickListeners(view);
        loadProfile();
        loadMyCollections();

        return view;
    }

    private void setupClickListeners(View view) {
        View rlUserInfo = view.findViewById(R.id.rl_user_info);
        View rlLibrary = view.findViewById(R.id.rl_library);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        if (rlUserInfo != null) {
            rlUserInfo.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ProfileEditActivity.class);
                editProfileLauncher.launch(intent);
            });
        }

        if (rlLibrary != null) {
            rlLibrary.setOnClickListener(v -> Toast.makeText(requireContext(), "Thư viện", Toast.LENGTH_SHORT).show());
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
            @Override public void onFailure(Call<UserProfile> call, Throwable t) {}
        });
    }

    private void loadMyCollections() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;

        RetrofitClient.getApi().getBoSuuTap(token).enqueue(new Callback<List<Collection>>() {
            @Override
            public void onResponse(Call<List<Collection>> call, Response<List<Collection>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    collectionList.clear();
                    collectionList.addAll(response.body());
                    collectionAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<List<Collection>> call, Throwable t) {}
        });
    }

    private void displayProfile(UserProfile profile) {
        tvUsername.setText(profile.getUsername());
        tvBio.setText(profile.getBio().isEmpty() ? "Chưa có mô tả" : profile.getBio());
        tvFollowers.setText(String.valueOf(profile.getFollowerCount()));
        tvFollowing.setText(String.valueOf(profile.getFollowingCount()));
        tvStoryCount.setText(String.valueOf(profile.getStoryCount()));

        Glide.with(this).load(profile.getAvatar())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .circleCrop().into(ivAvatar);
    }
}