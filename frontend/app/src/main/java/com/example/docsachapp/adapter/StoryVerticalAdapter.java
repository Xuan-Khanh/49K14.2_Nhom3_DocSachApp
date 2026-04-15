package com.example.docsachapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docsachapp.BookDetailsActivity;
import com.example.docsachapp.R;
import com.example.docsachapp.model.Story;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class StoryVerticalAdapter extends RecyclerView.Adapter<StoryVerticalAdapter.ViewHolder> {

    private List<Story> storyList;
    private Context context;

    public StoryVerticalAdapter(List<Story> storyList, Context context) {
        this.storyList = storyList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story_vertical, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Story story = storyList.get(position);
        
        holder.tvTitle.setText(story.getTitle());
        holder.tvAuthor.setText(story.getAuthorName());
        holder.tvViews.setText("👁 " + formatNumber(story.getViews()));
        // CẬP NHẬT: Hiển thị đúng số chương từ API
        holder.tvChapters.setText("  ≡ " + story.getChaptersCount() + " chương");
        holder.tvGenres.setText(story.getGenresText());

        Glide.with(context)
                .load(story.getCoverUrl())
                .placeholder(R.drawable.image5)
                .into(holder.ivCover);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetailsActivity.class);
            intent.putExtra("STORY_ID", story.getId());
            context.startActivity(intent);
        });
    }

    private String formatNumber(int number) {
        if (number >= 1000000) return String.format("%.1f M", number / 1000000.0);
        if (number >= 1000) return String.format("%.1f N", number / 1000.0);
        return String.valueOf(number);
    }

    @Override
    public int getItemCount() {
        return storyList != null ? storyList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView ivCover;
        TextView tvTitle, tvAuthor, tvViews, tvChapters, tvGenres;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvViews = itemView.findViewById(R.id.tv_views);
            tvChapters = itemView.findViewById(R.id.tv_chapters);
            tvGenres = itemView.findViewById(R.id.tv_genres);
        }
    }
}
