package com.example.docsachapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docsachapp.BookDetailsActivity;
import com.example.docsachapp.R;
import com.example.docsachapp.model.Story;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoryGridAdapter extends RecyclerView.Adapter<StoryGridAdapter.ViewHolder> {

    private List<Story> storyList;
    private Context context;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedStoryIds = new HashSet<>();
    private Set<Integer> alreadyAddedIds = new HashSet<>();
    private OnSelectionChangeListener selectionChangeListener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }

    public StoryGridAdapter(List<Story> storyList, Context context) {
        this.storyList = storyList;
        this.context = context;
    }

    public void setSelectionMode(boolean isMode) {
        this.isSelectionMode = isMode;
        if (!isMode) selectedStoryIds.clear();
        notifyDataSetChanged();
    }

    public void setAlreadyAddedIds(Set<Integer> ids) {
        this.alreadyAddedIds = ids;
        notifyDataSetChanged();
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    public Set<Integer> getSelectedStoryIds() {
        return selectedStoryIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Story story = storyList.get(position);
        holder.tvTitle.setText(story.getTitle());

        Glide.with(context)
                .load(story.getCoverUrl())
                .placeholder(R.drawable.image5)
                .into(holder.ivCover);

        // Hiển thị/Ẩn dấu tick chọn và lớp phủ tối
        boolean isSelected = selectedStoryIds.contains(story.getId());
        boolean isAlreadyAdded = alreadyAddedIds.contains(story.getId());

        if (isSelectionMode && (isSelected || isAlreadyAdded)) {
            holder.ivCheck.setVisibility(View.VISIBLE);
        } else {
            holder.ivCheck.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                // Nếu truyện đã được thêm rồi thì không cho chọn lại
                if (isAlreadyAdded) return;

                // Toggle chọn
                if (selectedStoryIds.contains(story.getId())) {
                    selectedStoryIds.remove(story.getId());
                } else {
                    selectedStoryIds.add(story.getId());
                }
                notifyItemChanged(position);
                if (selectionChangeListener != null) {
                    selectionChangeListener.onSelectionChanged(selectedStoryIds.size());
                }
            } else {
                // Chế độ xem chi tiết bình thường
                Intent intent = new Intent(context, BookDetailsActivity.class);
                intent.putExtra("STORY_ID", story.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyList != null ? storyList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView ivCover;
        ImageView ivCheck;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivCheck = itemView.findViewById(R.id.iv_check);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }
}