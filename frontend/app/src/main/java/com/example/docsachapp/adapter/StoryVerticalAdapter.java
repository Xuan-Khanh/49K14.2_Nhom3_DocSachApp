package com.example.docsachapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

        Glide.with(context)
                .load(story.getCoverUrl())
                .placeholder(R.drawable.image5)
                .into(holder.ivCover);

        // Hiển thị thể loại dạng thẻ bo tròn (Chip)
        if (holder.layoutGenres != null) {
            holder.layoutGenres.removeAllViews();
            List<Story.Genre> genres = story.getGenres();
            if (genres != null && !genres.isEmpty()) {
                // Chỉ hiển thị tối đa 2-3 thẻ để không bị tràn dòng
                int limit = Math.min(genres.size(), 3);
                for (int i = 0; i < limit; i++) {
                    View chipView = LayoutInflater.from(context).inflate(R.layout.layout_genre_chip_small, holder.layoutGenres, false);
                    TextView tvChip = chipView.findViewById(R.id.tv_chip_text);
                    tvChip.setText(genres.get(i).getName());
                    holder.layoutGenres.addView(chipView);
                }
            }
        }

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
        TextView tvTitle, tvAuthor, tvViews, tvChapters;
        LinearLayout layoutGenres;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvViews = itemView.findViewById(R.id.tv_views);
            tvChapters = itemView.findViewById(R.id.tv_chapters);
            layoutGenres = itemView.findViewById(R.id.layout_genres);
        }
    }
}
