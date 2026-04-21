package com.example.docsachapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.docsachapp.R;
import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {
    private List<String> list;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(String term);
        void onDelete(String term);
    }

    public RecentSearchAdapter(List<String> list, Context context, OnItemClickListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    public void updateData(List<String> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String term = list.get(position);
        holder.tvRecentText.setText(term);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(term);
        });

        holder.ivDeleteRecent.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(term);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecentText;
        ImageView ivDeleteRecent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecentText = itemView.findViewById(R.id.tv_recent_text);
            ivDeleteRecent = itemView.findViewById(R.id.iv_delete_recent);
        }
    }
}
