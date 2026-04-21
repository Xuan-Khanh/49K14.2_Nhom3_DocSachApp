package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchResultResponse {
    @SerializedName("keyword")
    private String keyword;

    @SerializedName("stories")
    private List<Story> stories;

    @SerializedName("users")
    private List<UserSearchItem> users;

    public String getKeyword() { return keyword; }
    public List<Story> getStories() { return stories; }
    public List<UserSearchItem> getUsers() { return users; }
}
