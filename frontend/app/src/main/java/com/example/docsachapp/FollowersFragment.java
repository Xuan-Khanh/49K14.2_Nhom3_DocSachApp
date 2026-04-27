package com.example.docsachapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.adapter.FollowListAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.UserSearchItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowersFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followers, container, false);

        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.main_container, new AdminFragment())
                        .commit();
            }
        });

        RecyclerView rv = view.findViewById(R.id.rv_followers);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        String token = new SessionManager(requireContext()).getAuthHeader();
        
        // ✅ FIX #7: Dùng FollowListAdapter thay vì UserFollowAdapter để có nút Follow/Unfollow
        RetrofitClient.getApi().getMyFollowers(token).enqueue(new Callback<List<UserSearchItem>>() {
            @Override
            public void onResponse(Call<List<UserSearchItem>> call, Response<List<UserSearchItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserSearchItem> list = response.body();
                    tvTitle.setText("Người theo dõi: " + list.size());
                    rv.setAdapter(new FollowListAdapter(list, requireContext()));
                }
            }

            @Override
            public void onFailure(Call<List<UserSearchItem>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải danh sách", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}

