package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Chapter.java
 * Map với JSON từ API:
 * GET /api/stories/{id}/chapters  → ChuongNgoanSerializer (gọn, không có noi_dung)
 * GET /api/chapters/{id}          → ChuongSerializer (đầy đủ, có noi_dung)
 */
public class Chapter {

    @SerializedName("id")
    private int id;

    @SerializedName("truyen_id")
    private int storyId;

    @SerializedName("ten_truyen")
    private String storyTitle;

    @SerializedName("tieu_de")
    private String title;

    @SerializedName("noi_dung")
    private String content;

    @SerializedName("trang_thai")
    private String status;

    @SerializedName("thoi_gian_tao")
    private String createdAt;

    @SerializedName("thoi_gian_dang")
    private String publishedAt;

    public int getId() { return id; }
    public int getStoryId() { return storyId; }
    public String getStoryTitle() { return storyTitle != null ? storyTitle : ""; }
    public String getTitle() { return title != null ? title : ""; }
    public String getContent() { return content != null ? content : ""; }
    public String getStatus() { return status != null ? status : ""; }
    public String getCreatedAt() { return createdAt; }
    public String getPublishedAt() { return publishedAt; }

    public boolean isPublished() {
        return "da_dang".equals(status);
    }
}
