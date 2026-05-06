package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class Story {

    @SerializedName("id")
    private int id;

    @SerializedName("ten_truyen")
    private String title;

    @SerializedName("mo_ta")
    private String description;

    @SerializedName("trang_thai")
    private String status;

    @SerializedName("trang_thai_display")
    private String statusDisplay;

    @SerializedName("cover_url") // ✅ Khớp với backend (link ảnh tuyệt đối)
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

    @SerializedName("followers") // ✅ Khớp với backend
    private int followerCount;

    @SerializedName("is_following")
    private boolean isFollowing;

    @SerializedName("user_rating")
    private int userRating;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructor mặc định cho GSON
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
    public int getFollowerCount() { return followerCount; }
    public boolean isFollowing() { return isFollowing; }
    public int getUserRating() { return userRating; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public String getStatusDisplay() {
        if (statusDisplay != null && !statusDisplay.isEmpty()) return statusDisplay;
        if (status == null) return "Không rõ";
        switch (status) {
            case "hoan_thanh": return "Hoàn thành";
            case "da_dang": return "Đang tiến hành";
            default: return status;
        }
    }

    public String getFormattedUpdatedAt() { return formatRelativeTime(updatedAt); }
    public String getFormattedCreatedAt() { return formatRelativeTime(createdAt); }

    private String formatRelativeTime(String isoTime) {
        if (isoTime == null || isoTime.isEmpty()) return "";
        try {
            String normalized = isoTime;
            if (normalized.matches(".*[+-]\\d{2}:\\d{2}$")) {
                int colonPos = normalized.lastIndexOf(':');
                normalized = normalized.substring(0, colonPos) + normalized.substring(colonPos + 1);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
            Date date = sdf.parse(normalized);
            if (date == null) return isoTime;
            long diffMs = System.currentTimeMillis() - date.getTime();
            long diffMin = diffMs / (60 * 1000);
            long diffHour = diffMs / (3600 * 1000);
            long diffDay = diffMs / (86400 * 1000L);
            if (diffMin < 1) return "Vừa xong";
            if (diffMin < 60) return diffMin + " phút trước";
            if (diffHour < 24) return diffHour + " giờ trước";
            if (diffDay < 30) return diffDay + " ngày trước";
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
        } catch (Exception e) { return isoTime; }
    }

    public String getAuthorName() { return author != null && author.getUsername() != null ? author.getUsername() : "Ẩn danh"; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Story story = (Story) o;
        return id == story.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

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
