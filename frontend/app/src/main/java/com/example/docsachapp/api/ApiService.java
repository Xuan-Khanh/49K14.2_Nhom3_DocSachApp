package com.example.docsachapp.api;

import com.example.docsachapp.model.*;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ==================== AUTH & PROFILE ====================
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @GET("auth/profile")
    Call<UserProfile> getUserProfile(@Header("Authorization") String authToken);

    @PUT("auth/profile")
    Call<Map<String, Object>> updateUserProfile(@Header("Authorization") String authToken, @Body Map<String, String> body);

    @GET("users/{id}")
    Call<UserProfile> getPublicProfile(@Path("id") int userId);

    @GET("users/{user_id}/collections")
    Call<List<Collection>> getUserCollections(@Path("user_id") int userId);

    // ==================== TRUYỆN ====================
    @GET("stories")
    Call<List<Story>> getStories(
            @Query("search") String search,
            @Query("theloai") Integer theLoaiId,
            @Query("trang_thai") String trangThai
    );

    @GET("stories")
    Call<List<Story>> searchStories(@Query("search") String keyword);

    @GET("stories/{id}")
    Call<Story> getStoryDetail(@Path("id") int id);

    @GET("stories/{story_id}/genres")
    Call<List<Story.Genre>> getStoryGenres(@Path("story_id") int storyId);

    // ==================== CHƯƠNG (Chapters) ====================
    @GET("stories/{id}/chapters")
    Call<List<Chapter>> getChapters(@Path("id") int storyId);

    @GET("chapters/{id}")
    Call<Chapter> getChapterDetail(@Path("id") int chapterId);

    // ==================== THEO DÕI TRUYỆN ====================
    @POST("follow/story")
    Call<Map<String, Object>> followStory(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    @HTTP(method = "DELETE", path = "unfollow/story", hasBody = true)
    Call<Map<String, Object>> unfollowStory(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    @GET("user/following-stories")
    Call<List<Story>> getFollowingStories(@Header("Authorization") String authToken);

    // ==================== BÌNH LUẬN ====================
    @GET("stories/{id}/comments")
    Call<List<Comment>> getComments(@Path("id") int storyId);

    @POST("comments")
    Call<Comment> postComment(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    // ==================== BỘ SƯU TẬP ====================
    @GET("bosuutap")
    Call<List<Collection>> getBoSuuTap(@Header("Authorization") String authToken);

    @GET("collections/{id}")
    Call<Collection> getCollectionDetail(@Header("Authorization") String authToken, @Path("id") int collectionId);

    @POST("collections")
    Call<Map<String, Object>> createCollection(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    @PUT("collections/{id}")
    Call<Map<String, Object>> updateCollection(@Header("Authorization") String authToken, @Path("id") int collectionId, @Body Map<String, Object> body);

    @DELETE("collections/{id}")
    Call<Map<String, Object>> deleteCollection(@Header("Authorization") String authToken, @Path("id") int collectionId);

    @POST("collections/add-story")
    Call<Map<String, Object>> addStoryToCollection(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    @HTTP(method = "DELETE", path = "collections/remove-story", hasBody = true)
    Call<Map<String, Object>> removeStoryFromCollection(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    // ==================== LỊCH SỬ & THỂ LOẠI ====================
    @GET("reading-history")
    Call<List<ReadingHistoryItem>> getReadingHistory(@Header("Authorization") String authToken);

    @POST("reading-history/update")
    Call<Map<String, Object>> updateReadingHistory(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    @GET("genres")
    Call<List<Story.Genre>> getGenres();
}
