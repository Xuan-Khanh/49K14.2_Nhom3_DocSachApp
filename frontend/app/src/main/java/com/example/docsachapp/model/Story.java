package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Story {

    @SerializedName("id")
    private int id;

    @SerializedName("ten_truyen")
    private String title;

    @SerializedName("mo_ta")
    private String description;

    @SerializedName("trang_thai")
    private String status;

    @SerializedName("anh_bia")
    private String coverUrl;

    @SerializedName("so_luot_doc")
    private int views;

    @SerializedName("tac_gia")
    private Author author;

    @SerializedName("the_loai")
    private List<Genre> genres;

    @SerializedName("diem_trung_binh")
    private float rating;

    @SerializedName("tong_danh_gia")
    private int totalRatings;

    @SerializedName("so_chuong")
    private int chaptersCount;

    @SerializedName("is_following")
    private boolean isFollowing;

    // Constructor mặc định cho GSON
    public Story() {}

    // Constructor để tạo nhanh từ lịch sử đọc
    public Story(int id, String title, String coverUrl) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
    }

    public int getId() { return id; }
    public String getTitle() { return title != null ? title : ""; }
    public String getDescription() { return description != null ? description : ""; }
    public String getStatus() { return status != null ? status : ""; }
    public String getCoverUrl() { return coverUrl; }
    public int getViews() { return views; }
    public Author getAuthor() { return author; }
    public List<Genre> getGenres() { return genres; }
    public float getRating() { return rating; }
    public int getTotalRatings() { return totalRatings; }
    public int getChaptersCount() { return chaptersCount; }
    public boolean isFollowing() { return isFollowing; }

    public String getAuthorName() {
        return author != null && author.getUsername() != null ? author.getUsername() : "Ẩn danh";
    }

    public String getGenresText() {
        if (genres == null || genres.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (Genre genre : genres) {
            if (genre != null && genre.getName() != null) {
                if (builder.length() > 0) builder.append(", ");
                builder.append(genre.getName());
            }
        }
        return builder.toString();
    }

    public static class Author {
        @SerializedName("id") private int id;
        @SerializedName("username") private String username;
        @SerializedName("avatar") private String avatar;
        public int getId() { return id; }
        public String getUsername() { return username != null ? username : ""; }
        public String getAvatar() { return avatar; }
    }

    public static class Genre {
        @SerializedName("id") private int id;
        @SerializedName("ten_the_loai") private String name;
        public int getId() { return id; }
        public String getName() { return name != null ? name : ""; }
    }
}