package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;

/**
 * UserProfile.java
 * Map với JSON từ API:
 * GET /api/auth/profile  → NguoiDungSerializer
 * PUT /api/auth/profile  → cùng response
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
 *   "so_following": 5
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
    private java.util.List<Story> publishedStories;

    @SerializedName("is_following")
    private boolean isFollowing;

    public int getId() { return id; }
    public String getUsername() { return username != null ? username : ""; }
    public String getEmail() { return email != null ? email : ""; }
    public String getAvatar() { return avatar; }
    public String getBirthday() { return birthday; }
    public String getBio() { return bio != null ? bio : ""; }
    public int getStoryCount() { return storyCount; }
    public int getFollowerCount() { return followerCount; }
    public int getFollowingCount() { return followingCount; }
    public java.util.List<Story> getPublishedStories() { return publishedStories; }
    public boolean isFollowing() { return isFollowing; }
    public void setFollowing(boolean following) { this.isFollowing = following; }
    public void setFollowerCount(int followerCount) { this.followerCount = followerCount; }
}
