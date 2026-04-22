package com.example.docsachapp.model;
import com.google.gson.annotations.SerializedName;
public class UserFollowItem {
    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("avatar")
    private String avatar;

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getAvatar() { return avatar; }
}
