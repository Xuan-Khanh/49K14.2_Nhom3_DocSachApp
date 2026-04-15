package com.example.docsachapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.example.docsachapp.model.ReadingHistoryItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.makeramen.roundedimageview.RoundedImageView;
import java.util.List;

public class ReadingHistoryAdapter extends RecyclerView.Adapter<ReadingHistoryAdapter.ViewHolder> {
    private List<ReadingHistoryItem> historyList;
    private Context context;
    private boolean isHistoryView; // Xác định xem đang hiển thị lịch sử hay danh sách hoàn thành

    public ReadingHistoryAdapter(List<ReadingHistoryItem> historyList, Context context, boolean isHistoryView) {
        this.historyList = historyList;
        this.context = context;
        this.isHistoryView = isHistoryView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recently_read, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReadingHistoryItem item = historyList.get(position);
        
        holder.tvTitle.setText(item.getTitle());
        holder.tvViews.setText("👁 " + formatNumber(item.getViews()));
        holder.tvFollowers.setText("🔖 " + formatNumber(item.getFollowers()));
        holder.tvChapters.setText("≡ " + item.getChaptersCount() + " chương");
        
        // XỬ LÝ LOGIC TRẠNG THÁI / TIẾN ĐỘ ĐỌC
        // Nếu là view lịch sử đọc -> Hiện "Đang xem: Chương..."
        if (isHistoryView) {
            if (item.getCurrentChapter() != null) {
                holder.tvCurrentChapter.setText("Đang xem: " + item.getCurrentChapter().getTitle());
                holder.tvCurrentChapter.setTextColor(Color.BLACK);
            } else {
                holder.tvCurrentChapter.setText("Chưa bắt đầu đọc");
                holder.tvCurrentChapter.setTextColor(Color.GRAY);
            }
        } 
        // Nếu không phải view lịch sử (ví dụ danh sách Hoàn thành) -> Hiện trạng thái truyện
        else {
             // Giả sử status được trả về trong ReadingHistoryItem hoặc ta hiển thị "Hoàn thành" 
             // nếu truyện đã kết thúc
             holder.tvCurrentChapter.setText("Trạng thái: Hoàn thành");
             holder.tvCurrentChapter.setTextColor(context.getResources().getColor(R.color.main_color)); // Dùng màu thương hiệu
             holder.tvCurrentChapter.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        // ChipGroup cho thể loại
        holder.cgGenres.removeAllViews();
        if (item.getGenres() != null) {
            for (String genreName : item.getGenres()) {
                Chip chip = new Chip(context);
                chip.setText(genreName);
                chip.setTextSize(10);
                chip.setChipMinHeight(60f);
                chip.setChipBackgroundColorResource(android.R.color.transparent);
                chip.setChipStrokeColorResource(R.color.grey_light);
                chip.setChipStrokeWidth(1f);
                holder.cgGenres.addView(chip);
            }
        }

        Glide.with(context)
                .load(item.getCoverUrl())
                .placeholder(R.drawable.image5)
                .into(holder.ivCover);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetailsActivity.class);
            intent.putExtra("STORY_ID", item.getBookId());
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
        return historyList != null ? historyList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView ivCover;
        TextView tvTitle, tvViews, tvFollowers, tvChapters, tvCurrentChapter;
        ChipGroup cgGenres;
        ImageView btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvViews = itemView.findViewById(R.id.tv_views);
            tvFollowers = itemView.findViewById(R.id.tv_followers);
            tvChapters = itemView.findViewById(R.id.tv_chapters);
            tvCurrentChapter = itemView.findViewById(R.id.tv_current_chapter);
            cgGenres = itemView.findViewById(R.id.cg_genres);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }
}
