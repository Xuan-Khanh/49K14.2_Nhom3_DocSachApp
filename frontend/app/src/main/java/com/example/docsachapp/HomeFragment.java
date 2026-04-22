package com.example.docsachapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.adapter.StoryHorizontalAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.ReadingHistoryItem;
import com.example.docsachapp.model.Story;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvNewStories, rvUpdateStories, rvCompletedStories, rvRecentStories;
    private StoryHorizontalAdapter newStoriesAdapter, updateStoriesAdapter, completedStoriesAdapter, recentStoriesAdapter;
    
    private List<Story> newStories = new ArrayList<>();
    private List<Story> updateStories = new ArrayList<>();
    private List<Story> completedStories = new ArrayList<>();
    private List<Story> recentStories = new ArrayList<>();
    
    private ProgressBar progressBar;
    private View scrollView;
    private TextView tvError;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        sessionManager = new SessionManager(requireContext());
        progressBar = view.findViewById(R.id.progress_bar);
        scrollView = view.findViewById(R.id.scroll_view);
        tvError = view.findViewById(R.id.tv_error);
        
        setupRecyclerViews(view);
        loadData();
        
        return view;
    }

    private void setupRecyclerViews(View view) {
        rvNewStories = view.findViewById(R.id.rv_new_stories);
        rvUpdateStories = view.findViewById(R.id.rv_update_stories);
        rvCompletedStories = view.findViewById(R.id.rv_completed_stories);
        rvRecentStories = view.findViewById(R.id.rv_recent_stories);

        newStoriesAdapter = new StoryHorizontalAdapter(newStories, getContext());
        updateStoriesAdapter = new StoryHorizontalAdapter(updateStories, getContext());
        completedStoriesAdapter = new StoryHorizontalAdapter(completedStories, getContext());
        recentStoriesAdapter = new StoryHorizontalAdapter(recentStories, getContext());

        rvNewStories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvUpdateStories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCompletedStories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecentStories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvNewStories.setAdapter(newStoriesAdapter);
        rvUpdateStories.setAdapter(updateStoriesAdapter);
        rvCompletedStories.setAdapter(completedStoriesAdapter);
        rvRecentStories.setAdapter(recentStoriesAdapter);
    }

    private void loadData() {
        showLoading(true);
        String token = sessionManager.getAuthHeader();
        
        // 1. Load Mới đăng & Mới cập nhật (FIXED: Thêm null cho tham số userId thứ 4)
        RetrofitClient.getApi().getStories(null, null, null, null).enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    newStories.clear();
                    newStories.addAll(response.body());
                    newStoriesAdapter.notifyDataSetChanged();

                    updateStories.clear();
                    updateStories.addAll(response.body());
                    updateStoriesAdapter.notifyDataSetChanged();
                    
                    showLoading(false);
                }
            }
            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {
                if (isAdded()) showError();
            }
        });

        // 2. Load Hoàn thành (FIXED: Thêm null cho tham số userId thứ 4)
        RetrofitClient.getApi().getStories(null, null, "hoan_thanh", null).enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    completedStories.clear();
                    completedStories.addAll(response.body());
                    completedStoriesAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {}
        });

        // 3. Load Đọc gần đây
        if (token != null) {
            RetrofitClient.getApi().getReadingHistory(token).enqueue(new Callback<List<ReadingHistoryItem>>() {
                @Override
                public void onResponse(Call<List<ReadingHistoryItem>> call, Response<List<ReadingHistoryItem>> response) {
                    if (isAdded() && response.isSuccessful() && response.body() != null) {
                        recentStories.clear();
                        for (ReadingHistoryItem item : response.body()) {
                            Story s = new Story(item.getBookId(), item.getTitle(), item.getCoverUrl());
                            recentStories.add(s);
                        }
                        recentStoriesAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onFailure(Call<List<ReadingHistoryItem>> call, Throwable t) {}
            });
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (scrollView != null) scrollView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if (tvError != null) tvError.setVisibility(View.GONE);
    }

    private void showError() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (scrollView != null) scrollView.setVisibility(View.GONE);
        if (tvError != null) tvError.setVisibility(View.VISIBLE);
    }
}
