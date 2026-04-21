package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;

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

<<<<<<< HEAD
=======
    @SerializedName("is_following")
    private boolean isFollowing;

    @SerializedName("user_rating")
    private int userRating;

    // Constructor mặc định cho GSON
>>>>>>> 9e9e0374c631cf9fdb04375ca856218821bba083
    public Story() {}

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
    public int getUserRating() { return userRating; }

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

    // ✅ FIX: Thêm hàm equals để hàm remove() trong List có thể tìm thấy truyện theo ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Story story = (Story) o;
        return id == story.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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