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
import com.example.docsachapp.CollectionDetailsActivity;
import com.example.docsachapp.R;
import com.example.docsachapp.model.Collection;
import com.example.docsachapp.model.Story;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private List<Collection> collectionList;
    private Context context;

    public CollectionAdapter(List<Collection> collectionList, Context context) {
        this.collectionList = collectionList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection collection = collectionList.get(position);
        holder.tvName.setText(collection.getName());
        holder.tvCount.setText(collection.getStoryCount() + " truyện");

        List<Story> stories = collection.getStories();
        if (stories != null && !stories.isEmpty()) {
            // Load main cover
            Glide.with(context)
                    .load(stories.get(0).getCoverUrl())
                    .placeholder(R.drawable.image5)
                    .into(holder.ivMain);

            // Load sub covers if exist
            if (stories.size() > 1) {
                Glide.with(context).load(stories.get(1).getCoverUrl()).placeholder(R.drawable.image5).into(holder.ivSub1);
            } else {
                holder.ivSub1.setImageResource(R.drawable.image5);
            }

            if (stories.size() > 2) {
                Glide.with(context).load(stories.get(2).getCoverUrl()).placeholder(R.drawable.image5).into(holder.ivSub2);
            } else {
                holder.ivSub2.setImageResource(R.drawable.image5);
            }
        } else {
            holder.ivMain.setImageResource(R.drawable.image5);
            holder.ivSub1.setImageResource(R.drawable.image5);
            holder.ivSub2.setImageResource(R.drawable.image5);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CollectionDetailsActivity.class);
            intent.putExtra("COLLECTION_ID", collection.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return collectionList != null ? collectionList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView ivMain, ivSub1, ivSub2;
        TextView tvName, tvCount;
        ImageView btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMain = itemView.findViewById(R.id.iv_cover_main);
            ivSub1 = itemView.findViewById(R.id.iv_cover_sub1);
            ivSub2 = itemView.findViewById(R.id.iv_cover_sub2);
            tvName = itemView.findViewById(R.id.tv_collection_name);
            tvCount = itemView.findViewById(R.id.tv_story_count);
            btnMore = itemView.findViewById(R.id.btn_collection_more);
        }
    }
}