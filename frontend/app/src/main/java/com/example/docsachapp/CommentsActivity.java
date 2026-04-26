package com.example.docsachapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Comment;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CommentsActivity – Màn hình bình luận
 *
 * Luồng:
 * 1. Nhận STORY_ID từ BookDetailsActivity
 * 2. Load danh sách bình luận từ GET /api/stories/{id}/comments
 * 3. Gửi bình luận mới qua POST /api/comments (cần token)
 */
public class CommentsActivity extends AppCompatActivity {

    private int storyId = -1;
    private RecyclerView rvComments;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private EditText etComment;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        storyId = getIntent().getIntExtra("STORY_ID", -1);
        if (storyId == -1) {
            Toast.makeText(this, "Lỗi: không xác định truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sessionManager = new SessionManager(this);
        initViews();
        loadComments();
    }

    private void initViews() {
        ImageView btnBack  = findViewById(R.id.btn_back);
        etComment          = findViewById(R.id.et_comment);
        ImageView btnSend  = findViewById(R.id.btn_send);
        rvComments         = findViewById(R.id.rv_comments);
        progressBar        = findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        adapter = new CommentAdapter(commentList);
        if (rvComments != null) {
            rvComments.setLayoutManager(new LinearLayoutManager(this));
            rvComments.setAdapter(adapter);
        }

        // Gửi bình luận
        btnSend.setOnClickListener(v -> sendComment());
    }

    // ─── Load danh sách bình luận ─────────────────────────────────────────────
    private void loadComments() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getApi().getComments(storyId).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    commentList.clear();
                    commentList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(CommentsActivity.this, "Lỗi tải bình luận", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Gửi bình luận ────────────────────────────────────────────────────────
    private void sendComment() {
        String token = sessionManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = etComment.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Chưa nhập nội dung bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("truyen_id", storyId);
        body.put("noi_dung", text);

        RetrofitClient.getApi().postComment(token, body).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    etComment.setText("");
                    // Thêm bình luận mới vào đầu danh sách
                    commentList.add(0, response.body());
                    adapter.notifyItemInserted(0);
                    if (rvComments != null) rvComments.scrollToPosition(0);
                    Toast.makeText(CommentsActivity.this, "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
                } else {
                    String msg = response.code() == 401
                            ? "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại"
                            : "Gửi thất bại (mã " + response.code() + ")";
                    Toast.makeText(CommentsActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                Toast.makeText(CommentsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Inline Adapter cho bình luận ─────────────────────────────────────────
    private static class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {
        private final List<Comment> list;

        CommentAdapter(List<Comment> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Comment c = list.get(position);
            holder.tvUsername.setText(c.getAuthorName());
            holder.tvContent.setText(c.getContent());

            // Load avatar nếu có
            if (holder.ivAvatar != null) {
                Glide.with(holder.itemView.getContext())
                        .load(c.getAuthorAvatar())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .circleCrop()
                        .into(holder.ivAvatar);
            }

            View.OnClickListener authorClickListener = v -> {
                if (c.getAuthor() != null) {
                    android.content.Intent intent = new android.content.Intent(v.getContext(), com.example.docsachapp.AuthorProfileActivity.class);
                    intent.putExtra("USER_ID", c.getAuthor().getId());
                    v.getContext().startActivity(intent);
                }
            };

            holder.tvUsername.setOnClickListener(authorClickListener);
            if (holder.ivAvatar != null) {
                holder.ivAvatar.setOnClickListener(authorClickListener);
            }
        }

        @Override
        public int getItemCount() { return list != null ? list.size() : 0; }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvUsername, tvContent;
            RoundedImageView ivAvatar;

            VH(@NonNull View itemView) {
                super(itemView);
                tvUsername = itemView.findViewById(R.id.tv_username);
                tvContent  = itemView.findViewById(R.id.tv_content);
                ivAvatar   = itemView.findViewById(R.id.iv_avatar);
            }
        }
    }
}
