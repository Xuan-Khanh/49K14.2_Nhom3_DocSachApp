package com.example.docsachapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.UserFollowItem;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.TextView;

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
        RetrofitClient.getApi().getFollowers(token).enqueue(new Callback<List<UserFollowItem>>() {
            @Override
            public void onResponse(Call<List<UserFollowItem>> call, Response<List<UserFollowItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserFollowItem> list = response.body();
                    tvTitle.setText("Người theo dõi: " + list.size());
                    rv.setAdapter(new UserFollowAdapter(list));
                }
            }

            @Override
            public void onFailure(Call<List<UserFollowItem>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải danh sách", Toast.LENGTH_SHORT).show();
            }
        });

        return view;  // thêm dòng này
    }  // đóng onCreateView
}  // đóng class