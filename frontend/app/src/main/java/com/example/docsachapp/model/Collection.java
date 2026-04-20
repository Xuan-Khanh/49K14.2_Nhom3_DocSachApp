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

    @SerializedName("so_truyen") // Trường nhận số lượng từ Backend
    private int storyCountFromApi;

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Story> getStories() { return stories; }
    
    /**
     * Hàm đếm thông minh: 
     * 1. Ưu tiên lấy số lượng từ Backend trả về (storyCountFromApi)
     * 2. Nếu không có, tự đếm số phần tử trong danh sách (stories.size)
     */
    public int getStoryCount() {
        if (storyCountFromApi > 0) {
            return storyCountFromApi;
        }
        return stories != null ? stories.size() : 0;
    }
}
