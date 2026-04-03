package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * ReadingHistoryItem.java
 * Map với JSON từ API:
 * GET /api/reading-history  → LichSuDocSerializer
 *
 * JSON mỗi phần tử:
 * {
 *   "id": 1,
 *   "book_id": 5,
 *   "title": "Tên truyện",
 *   "author": "username_tac_gia",
 *   "cover_url": "http://...",
 *   "views": 100,
 *   "followers": 20,
 *   "chapters_count": 10,
 *   "genres": ["Action", "Romance"],
 *   "current_chapter": { "id": 3, "title": "Chương 3" },
 *   "updated_at": "2024-01-01T08:00:00Z"
 * }
 */
public class ReadingHistoryItem {

    @SerializedName("id")
    private int id;

    @SerializedName("book_id")
    private int bookId;

    @SerializedName("title")
    private String title;

    @SerializedName("author")
    private String author;

    @SerializedName("cover_url")
    private String coverUrl;

    @SerializedName("views")
    private int views;

    @SerializedName("followers")
    private int followers;

    @SerializedName("chapters_count")
    private int chaptersCount;

    @SerializedName("genres")
    private List<String> genres;

    @SerializedName("current_chapter")
    private CurrentChapter currentChapter;

    @SerializedName("updated_at")
    private String updatedAt;

    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public String getTitle() { return title != null ? title : ""; }
    public String getAuthor() { return author != null ? author : "Ẩn danh"; }
    public String getCoverUrl() { return coverUrl; }
    public int getViews() { return views; }
    public int getFollowers() { return followers; }
    public int getChaptersCount() { return chaptersCount; }
    public List<String> getGenres() { return genres; }
    public CurrentChapter getCurrentChapter() { return currentChapter; }
    public String getUpdatedAt() { return updatedAt; }

    public String getGenresText() {
        if (genres == null || genres.isEmpty()) return "";
        return String.join(", ", genres);
    }

    /** Chuyển ReadingHistoryItem thành Story tương thích với adapter cũ */
    public Story toStory() {
        // Không thể tạo Story trực tiếp vì field private
        // Dùng class này trực tiếp với adapter mới
        return null;
    }

    public static class CurrentChapter {
        @SerializedName("id")
        private int id;

        @SerializedName("title")
        private String title;

        public int getId() { return id; }
        public String getTitle() { return title != null ? title : ""; }
    }
}
