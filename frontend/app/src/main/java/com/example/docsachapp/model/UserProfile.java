package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * UserProfile.java
 * Map với JSON từ API:
 * GET /api/auth/profile  → NguoiDungSerializer
 * PUT /api/auth/profile  → cùng response
 * GET /api/users/{id}/   → trả thêm is_self, truyen_da_dang, truyen_hoan_thanh
 *
 * JSON:
 * {
 *   "id": 1,
 *   "username": "abc",
 *   "email": "abc@mail.com",
 *   "avatar": "http://.../media/avatars/...",
 *   "ngay_sinh": "2000-01-01",
 *   "mo_ta": "...",
 *   "so_truyen": 3,
 *   "so_follower": 10,
 *   "so_following": 5,
 *   "is_self": false,
 *   "is_following": true,
 *   "truyen_da_dang": [...],
 *   "truyen_hoan_thanh": [...]
 * }
 */
public class UserProfile {

    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("ngay_sinh")
    private String birthday;

    @SerializedName("mo_ta")
    private String bio;

    @SerializedName("so_truyen")
    private int storyCount;

    @SerializedName("so_follower")
    private int followerCount;

    @SerializedName("so_following")
    private int followingCount;

    @SerializedName("truyen_da_dang")
    private List<Story> publishedStories;

    @SerializedName("truyen_hoan_thanh")
    private List<Story> completedStories;

    @SerializedName("is_following")
    private boolean isFollowing;

    @SerializedName("is_self")
    private boolean isSelf;

    public int getId() { return id; }
    public String getUsername() { return username != null ? username : ""; }
    public String getEmail() { return email != null ? email : ""; }
    public String getAvatar() { return avatar; }
    public String getBirthday() { return birthday; }
    public String getBio() { return bio != null ? bio : ""; }
    public int getStoryCount() { return storyCount; }
    public int getFollowerCount() { return followerCount; }
    public int getFollowingCount() { return followingCount; }
    public List<Story> getPublishedStories() { return publishedStories; }
    public List<Story> getCompletedStories() { return completedStories; }
    public boolean isFollowing() { return isFollowing; }
    public void setFollowing(boolean following) { this.isFollowing = following; }
    public void setFollowerCount(int followerCount) { this.followerCount = followerCount; }
    public boolean isSelf() { return isSelf; }

    /**
     * Trả về danh sách tất cả truyện (da_dang + hoan_thanh).
     * Dùng cho hiển thị trên profile người dùng để không bị thiếu truyện hoàn thành.
     */
    public List<Story> getAllStories() {
        List<Story> all = new ArrayList<>();
        if (publishedStories != null) all.addAll(publishedStories);
        if (completedStories != null) all.addAll(completedStories);
        return all;
    }
}
