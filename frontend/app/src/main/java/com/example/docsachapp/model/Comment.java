package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Comment.java
 * Map với JSON từ API:
 * GET /api/stories/{id}/comments  → BinhLuanSerializer
 *
 * JSON:
 * {
 *   "id": 1,
 *   "truyen_id": 5,       (write-only, không có trong response list)
 *   "nguoi_dung_info": { "id": 1, "username": "abc", "avatar": null },
 *   "noi_dung": "Hay quá!",
 *   "thoi_gian_bl": "2024-01-01T08:00:00Z"
 * }
 */
public class Comment {

    @SerializedName("id")
    private int id;

    @SerializedName("nguoi_dung_info")
    private AuthorInfo author;

    @SerializedName("noi_dung")
    private String content;

    @SerializedName("thoi_gian_bl")
    private String createdAt;

    public int getId() { return id; }
    public AuthorInfo getAuthor() { return author; }
    public String getContent() { return content != null ? content : ""; }
    public String getCreatedAt() { return createdAt; }

    public String getAuthorName() {
        return author != null && author.getUsername() != null ? author.getUsername() : "Ẩn danh";
    }

    public String getAuthorAvatar() {
        return author != null ? author.getAvatar() : null;
    }

    public static class AuthorInfo {
        @SerializedName("id")
        private int id;

        @SerializedName("username")
        private String username;

        @SerializedName("avatar")
        private String avatar;

        public int getId() { return id; }
        public String getUsername() { return username != null ? username : ""; }
        public String getAvatar() { return avatar; }
    }
}
