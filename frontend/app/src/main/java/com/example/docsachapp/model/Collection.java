package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Collection {
    @SerializedName("id")
    private int id;

    @SerializedName("ten_bo_suu_tap")
    private String name;

    @SerializedName("truyen_list")
    private List<Story> stories;

    public int getId() { return id; }
    public String getName() { return name; }
    public List<Story> getStories() { return stories; }
    
    public int getStoryCount() {
        return stories != null ? stories.size() : 0;
    }
}
