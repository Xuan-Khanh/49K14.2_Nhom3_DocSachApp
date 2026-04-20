package com.example.docsachapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docsachapp.BookDetailsActivity;
import com.example.docsachapp.R;
import com.example.docsachapp.model.Story;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class StoryManageAdapter extends RecyclerView.Adapter<StoryManageAdapter.ViewHolder> {

    private List<Story> storyList;
    private Context context;
    private OnStoryActionListener listener;

    public interface OnStoryActionListener {
        void onDeleteStory(Story story); // Truyền đối tượng thay vì position
    }

    public StoryManageAdapter(List<Story> storyList, Context context, OnStoryActionListener listener) {
        this.storyList = storyList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story_vertical_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Story story = storyList.get(position);
        
        holder.tvTitle.setText(story.getTitle());
        holder.tvAuthor.setText(story.getAuthorName());
        holder.tvViews.setText("👁 " + story.getViews());
        holder.tvChapters.setText("  ≡ " + story.getChaptersCount() + " chương");

        Glide.with(context)
                .load(story.getCoverUrl())
                .placeholder(R.drawable.image5)
                .into(holder.ivCover);

        if (holder.layoutGenres != null) {
            holder.layoutGenres.removeAllViews();
            List<Story.Genre> genres = story.getGenres();
            if (genres != null) {
                int limit = Math.min(genres.size(), 2);
                for (int i = 0; i < limit; i++) {
                    View chipView = LayoutInflater.from(context).inflate(R.layout.layout_genre_chip_small, holder.layoutGenres, false);
                    ((TextView)chipView.findViewById(R.id.tv_chip_text)).setText(genres.get(i).getName());
                    holder.layoutGenres.addView(chipView);
                }
            }
        }

        holder.btnItemMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnItemMore);
            popup.getMenu().add("Xóa khỏi bộ sưu tập");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Xóa khỏi bộ sưu tập")) {
                    if (listener != null) listener.onDeleteStory(story);
                }
                return true;
            });
            popup.show();
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetailsActivity.class);
            intent.putExtra("STORY_ID", story.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return storyList != null ? storyList.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView ivCover;
        TextView tvTitle, tvAuthor, tvViews, tvChapters;
        ImageView btnItemMore;
        LinearLayout layoutGenres;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvViews = itemView.findViewById(R.id.tv_views);
            tvChapters = itemView.findViewById(R.id.tv_chapters);
            btnItemMore = itemView.findViewById(R.id.btn_item_more);
            layoutGenres = itemView.findViewById(R.id.layout_genres);
        }
    }
}