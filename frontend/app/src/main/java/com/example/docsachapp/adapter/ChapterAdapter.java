package com.example.docsachapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.R;
import com.example.docsachapp.model.Chapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private List<Chapter> chapters;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Chapter chapter);
    }

    public ChapterAdapter(List<Chapter> chapters, Context context, OnItemClickListener listener) {
        this.chapters = chapters;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = chapters.get(position);
        holder.tvTitle.setText(chapter.getTitle());

        // Định dạng ngày tháng năm: Chỉ lấy dd/MM/yyyy
        String formattedDate = formatDate(chapter.getPublishedAt());
        holder.tvDate.setText(formattedDate);
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(chapter));
    }

    private String formatDate(String dateStr) {
        if (dateDate(dateStr) == null) return "";
        try {
            // Giả sử định dạng từ server là ISO 8601 (yyyy-MM-dd'T'HH:mm:ss...)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateStr.substring(0, 10));
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr; // Nếu lỗi thì hiện chuỗi gốc
        }
    }

    private String dateDate(String s) {
        return (s != null && s.length() >= 10) ? s : null;
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_chapter_title);
            tvDate = itemView.findViewById(R.id.tv_chapter_date);
        }
    }
}