package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;

public class UserSearchItem {
    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("so_truyen")
    private int soTruyen;

    @SerializedName("mo_ta")
    private String moTa;

    @SerializedName("is_following")
    private boolean isFollowing;

    public int getId() { return id; }
    public String getUsername() { return username != null ? username : ""; }
    public String getAvatar() { return avatar; }
    public int getSoTruyen() { return soTruyen; }
    public String getMoTa() { return moTa != null ? moTa : ""; }
    public boolean isFollowing() { return isFollowing; }
    public void setFollowing(boolean following) { isFollowing = following; }
}
