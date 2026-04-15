package com.example.docsachapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.docsachapp.R;
import com.example.docsachapp.BookDetailsActivity;
import com.example.docsachapp.model.Story;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.makeramen.roundedimageview.RoundedImageView;
import java.util.List;

public class StorySearchAdapter extends RecyclerView.Adapter<StorySearchAdapter.ViewHolder> {
    private List<Story> list;
    private Context context;

    public StorySearchAdapter(List<Story> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void updateData(List<Story> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Story story = list.get(position);
        holder.tvTitle.setText(story.getTitle() + " - " + story.getAuthorName());
        
        if ("da_hoan_thanh".equals(story.getStatus())) {
            holder.tvBadge.setVisibility(View.VISIBLE);
            holder.tvBadge.setText("Đã hoàn thành");
            holder.tvBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_yellow));
        } else if ("dang_tien_hanh".equals(story.getStatus())){
            holder.tvBadge.setVisibility(View.VISIBLE);
            holder.tvBadge.setText("Đang tiến hành");
            // Optionally, different color for "dang_tien_hanh"
        } else {
            holder.tvBadge.setVisibility(View.GONE);
        }

        holder.tvViews.setText("👁 " + story.getViews());
        holder.tvBookmarks.setText("⚑ " + story.getTotalRatings()); // Fallback to totalRatings if bookmarks not available
        holder.tvChapters.setText("≡ " + story.getChaptersCount() + " chương");
        holder.tvDescription.setText(story.getDescription());

        // Load image
        if (story.getCoverUrl() != null && !story.getCoverUrl().isEmpty()) {
            String fullUrl = story.getCoverUrl().startsWith("http") ? story.getCoverUrl() 
                             : "http://10.0.2.2:8000" + story.getCoverUrl();
            Glide.with(context).load(fullUrl).into(holder.ivCover);
        }

        // Tags
        holder.cgTags.removeAllViews();
        if (story.getGenres() != null) {
            for (Story.Genre genre : story.getGenres()) {
                Chip chip = new Chip(context);
                chip.setText(genre.getName());
                chip.setClickable(false);
                chip.setCheckable(false);
                chip.setTextAppearanceResource(android.R.style.TextAppearance_Material_Small); // Use smaller text block
                holder.cgTags.addView(chip);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetailsActivity.class);
            intent.putExtra("STORY_ID", story.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView ivCover;
        TextView tvTitle, tvBadge, tvViews, tvBookmarks, tvChapters, tvDescription;
        ChipGroup cgTags;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvBadge = itemView.findViewById(R.id.tv_badge);
            tvViews = itemView.findViewById(R.id.tv_views);
            tvBookmarks = itemView.findViewById(R.id.tv_bookmarks);
            tvChapters = itemView.findViewById(R.id.tv_chapters);
            tvDescription = itemView.findViewById(R.id.tv_description);
            cgTags = itemView.findViewById(R.id.cg_tags);
        }
    }
}
