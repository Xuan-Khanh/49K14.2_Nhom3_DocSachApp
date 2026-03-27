package com.example.docsachapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AdminFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        TextView btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        TextView btnFollowers = view.findViewById(R.id.btn_followers);
        TextView btnLogout = view.findViewById(R.id.btn_logout);

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ProfileEditActivity.class));
        });

        btnFollowers.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Danh sách người theo dõi (Mock)", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có muốn đăng xuất khỏi ứng dụng?")
                .setPositiveButton("Đồng ý", (d, w) -> {
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    // Clear task to prevent going back
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", null)
                .show();
        });

        return view;
    }
}
