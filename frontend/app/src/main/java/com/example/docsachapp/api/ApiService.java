package com.example.docsachapp.api;

import com.example.docsachapp.model.LoginRequest;
import com.example.docsachapp.model.LoginResponse;
import com.example.docsachapp.model.RegisterRequest;
import com.example.docsachapp.model.RegisterResponse;
import com.example.docsachapp.model.Story;
import com.example.docsachapp.model.UserProfile;
import com.example.docsachapp.model.Comment;
import com.example.docsachapp.model.ReadingHistoryItem;
import com.example.docsachapp.model.Chapter;
import com.example.docsachapp.model.Collection;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
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

    @POST("users/follow")
    Call<java.util.Map<String, Object>> followUser(@Header("Authorization") String authToken, @Body java.util.Map<String, Integer> body);

    @retrofit2.http.HTTP(method = "DELETE", path = "users/unfollow", hasBody = true)
    Call<java.util.Map<String, Object>> unfollowUser(@Header("Authorization") String authToken, @Body java.util.Map<String, Integer> body);

    @GET("users/{id}/followers")
    Call<java.util.List<com.example.docsachapp.model.UserSearchItem>> getFollowers(@Path("id") int userId);

    @GET("users/{id}/following")
    Call<java.util.List<com.example.docsachapp.model.UserSearchItem>> getFollowing(@Path("id") int userId);

    // ==================== TRUYỆN ====================

    @GET("stories")
    Call<List<Story>> getStories(
            @Query("search") String search,
            @Query("theloai") Integer theLoaiId,
            @Query("trang_thai") String trangThai
    );

    @GET("stories")
    Call<List<Story>> searchStories(@Query("search") String keyword);

    @GET("search")
    Call<com.example.docsachapp.model.SearchResultResponse> searchAll(@Query("keyword") String keyword);

    @GET("genres")
    Call<List<Story.Genre>> getGenres();

    @GET("stories/{id}")
    Call<Story> getStoryDetail(@Path("id") int id);

    @GET("stories/{id}/chapters")
    Call<List<Chapter>> getChapters(@Path("id") int storyId);

    @GET("chapters/{id}")
    Call<Chapter> getChapterDetail(@Path("id") int chapterId);

    // ==================== THEO DÕI ====================

    @GET("user/following-stories")
    Call<List<Story>> getFollowingStories(@Header("Authorization") String authToken);

    @POST("user/follow-story")
    Call<Map<String, Object>> followStory(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    @POST("user/unfollow-story")
    Call<Map<String, Object>> unfollowStory(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    // ==================== LỊCH SỬ ĐỌC ====================

    @GET("reading-history")
    Call<List<ReadingHistoryItem>> getReadingHistory(@Header("Authorization") String authToken);

    @POST("reading-history/update")
    Call<Map<String, Object>> updateReadingHistory(@Header("Authorization") String authToken, @Body Map<String, Object> body);

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
}
