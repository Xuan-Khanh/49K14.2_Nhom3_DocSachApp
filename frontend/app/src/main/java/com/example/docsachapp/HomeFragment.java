package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docsachapp.adapter.StoryHorizontalAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.ReadingHistoryItem;
import com.example.docsachapp.model.Story;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvNewStories, rvCompletedStories;
    private StoryHorizontalAdapter newStoriesAdapter, completedStoriesAdapter;
    private List<Story> newStories = new ArrayList<>();
    private List<Story> completedStories = new ArrayList<>();

    // Recent history hiển thị theo dạng khác (ReadingHistoryItem)
    private RecyclerView rvRecentStories;
    private HistoryAdapter recentAdapter;
    private List<ReadingHistoryItem> recentItems = new ArrayList<>();

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
        rvCompletedStories = view.findViewById(R.id.rv_completed_stories);
        rvRecentStories = view.findViewById(R.id.rv_recent_stories);

        newStoriesAdapter = new StoryHorizontalAdapter(newStories, getContext());
        completedStoriesAdapter = new StoryHorizontalAdapter(completedStories, getContext());
        recentAdapter = new HistoryAdapter(recentItems, getContext());

        rvNewStories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCompletedStories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecentStories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvNewStories.setAdapter(newStoriesAdapter);
        rvCompletedStories.setAdapter(completedStoriesAdapter);
        rvRecentStories.setAdapter(recentAdapter);
    }

    private void loadData() {
        showLoading(true);

        // Load New Stories (tất cả truyện đã đăng)
        RetrofitClient.getApi().getStories(null, null, null).enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    newStories.clear();
                    newStories.addAll(response.body());
                    newStoriesAdapter.notifyDataSetChanged();
                    showLoading(false);
                } else if (isAdded()) {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {
                if (isAdded()) showError();
            }
        });

        // Load Completed Stories (trạng thái hoan_thanh - FIX: đúng giá trị API)
        RetrofitClient.getApi().getStories(null, null, "hoan_thanh").enqueue(new Callback<List<Story>>() {
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

        // Load Reading History (FIX: dùng đúng kiểu ReadingHistoryItem)
        String token = sessionManager.getAuthHeader();
        if (token != null) {
            RetrofitClient.getApi().getReadingHistory(token).enqueue(new Callback<List<ReadingHistoryItem>>() {
                @Override
                public void onResponse(Call<List<ReadingHistoryItem>> call, Response<List<ReadingHistoryItem>> response) {
                    if (isAdded() && response.isSuccessful() && response.body() != null) {
                        recentItems.clear();
                        recentItems.addAll(response.body());
                        recentAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<List<ReadingHistoryItem>> call, Throwable t) {}
            });
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        scrollView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    private void showError() {
        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
    }

    // ─── Inline Adapter cho Reading History ───────────────────────────────────
    /**
     * HistoryAdapter hiển thị ReadingHistoryItem (lịch sử đọc) trong RecyclerView ngang.
     * Dùng cùng layout item_story_horizontal – chỉ hiện cover + title.
     */
    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        private final List<ReadingHistoryItem> list;
        private final android.content.Context ctx;

        HistoryAdapter(List<ReadingHistoryItem> list, android.content.Context ctx) {
            this.list = list;
            this.ctx = ctx;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_story_horizontal, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ReadingHistoryItem item = list.get(position);
            holder.tvTitle.setText(item.getTitle());
            Glide.with(ctx).load(item.getCoverUrl()).placeholder(R.drawable.image5).into(holder.ivCover);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, BookDetailsActivity.class);
                intent.putExtra("STORY_ID", item.getBookId());
                ctx.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return list != null ? list.size() : 0; }

        static class VH extends RecyclerView.ViewHolder {
            RoundedImageView ivCover;
            TextView tvTitle;
            VH(@NonNull View itemView) {
                super(itemView);
                ivCover = itemView.findViewById(R.id.iv_cover);
                tvTitle = itemView.findViewById(R.id.tv_title);
            }
        }
    }
}