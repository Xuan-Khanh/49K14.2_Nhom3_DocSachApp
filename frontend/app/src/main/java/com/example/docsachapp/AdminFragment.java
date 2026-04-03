package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AdminFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        // Get views from layout
        android.view.View rlUserInfo = view.findViewById(R.id.rl_user_info);
        android.view.View rlNotifications = view.findViewById(R.id.rl_notifications);
        android.view.View rlLibrary = view.findViewById(R.id.rl_library);
        android.view.View rlComments = view.findViewById(R.id.rl_comments);

        // User info click listener - open profile edit
        if (rlUserInfo != null) {
            rlUserInfo.setOnClickListener(v -> startActivity(new Intent(requireContext(), ProfileEditActivity.class)));
        }

        // Notifications click listener
        if (rlNotifications != null) {
            rlNotifications.setOnClickListener(v -> Toast.makeText(requireContext(), "Thông báo của tôi", Toast.LENGTH_SHORT).show());
        }

        // Library click listener
        if (rlLibrary != null) {
            rlLibrary.setOnClickListener(v -> Toast.makeText(requireContext(), "Thư viện của tôi", Toast.LENGTH_SHORT).show());
        }

        // Comments click listener
        if (rlComments != null) {
            rlComments.setOnClickListener(v -> Toast.makeText(requireContext(), "Bình luận của tôi", Toast.LENGTH_SHORT).show());
        }

        return view;
    }
}
