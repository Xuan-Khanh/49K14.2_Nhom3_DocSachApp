package com.example.docsachapp.api;

import com.example.docsachapp.model.*;
import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
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
    Call<UserProfile> getPublicProfile(@Path("id") int userId, @Header("Authorization") String authToken);

    @POST("users/follow")
    Call<Map<String, Object>> followUser(@Header("Authorization") String authToken, @Body Map<String, Integer> body);

    @HTTP(method = "DELETE", path = "users/unfollow", hasBody = true)
    Call<Map<String, Object>> unfollowUser(@Header("Authorization") String authToken, @Body Map<String, Integer> body);

    @GET("users/{id}/followers")
    Call<List<UserSearchItem>> getFollowers(@Path("id") int userId, @Header("Authorization") String authToken);

    @GET("users/{id}/following")
    Call<List<UserSearchItem>> getFollowing(@Path("id") int userId, @Header("Authorization") String authToken);

    @GET("users/{user_id}/collections")
    Call<List<Collection>> getUserCollections(@Path("user_id") int userId);

    // ==================== TRUYỆN (Stories) ====================
    @GET("stories")
    Call<List<Story>> getStories(
            @Query("search") String search,
            @Query("theloai") Integer theLoaiId,
            @Query("trang_thai") String trangThai,
            @Query("nguoi_dung_id") Integer userId
    );

    @Multipart
    @POST("stories")
    Call<Story> createStory(
            @Header("Authorization") String authToken,
            @Part("ten_truyen") RequestBody title,
            @Part("mo_ta") RequestBody description,
            @Part("trang_thai") RequestBody status,
            @Part List<MultipartBody.Part> the_loai,
            @Part MultipartBody.Part coverImage
    );

    @Multipart
    @PUT("stories/{id}")
    Call<Story> updateStoryMultipart(
            @Header("Authorization") String authToken,
            @Path("id") int storyId,
            @Part("ten_truyen") RequestBody title,
            @Part("mo_ta") RequestBody description,
            @Part("trang_thai") RequestBody status,
            @Part List<MultipartBody.Part> the_loai,
            @Part MultipartBody.Part coverImage
    );

    @PUT("stories/{id}")
    Call<Story> updateStory(@Header("Authorization") String authToken, @Path("id") int storyId, @Body Map<String, Object> body);

    @DELETE("stories/{id}")
    Call<Map<String, Object>> deleteStory(@Header("Authorization") String authToken, @Path("id") int storyId);

    @GET("stories/my-stories")
    Call<List<Story>> getMyStories(@Header("Authorization") String authToken);

    @GET("search")
    Call<SearchResultResponse> searchAll(@Query("keyword") String keyword);

    @GET("genres")
    Call<List<Story.Genre>> getGenres();

    @GET("stories/{id}")
    Call<Story> getStoryDetail(@Header("Authorization") String authToken, @Path("id") int id);

    @GET("stories/{id}/chapters")
    Call<List<Chapter>> getChapters(@Header("Authorization") String authToken, @Path("id") int storyId);

    @GET("chapters/{id}/detail")
    Call<Chapter> getChapterDetail(@Header("Authorization") String authToken, @Path("id") int chapterId);

    @POST("chapters")
    Call<Chapter> createChapter(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    @PUT("chapters/{id}")
    Call<Chapter> updateChapter(@Header("Authorization") String authToken, @Path("id") int chapterId, @Body Map<String, Object> body);

    @DELETE("chapters/{id}")
    Call<Map<String, Object>> deleteChapter(@Header("Authorization") String authToken, @Path("id") int chapterId);

    @POST("chapters/batch-action")
    Call<Map<String, Object>> batchActionChapters(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    // ==================== THEO DÕI & BÌNH LUẬN ====================
    @POST("follow/story")
    Call<Map<String, Object>> followStory(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    @HTTP(method = "DELETE", path = "unfollow/story", hasBody = true)
    Call<Map<String, Object>> unfollowStory(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    @GET("user/following-stories")
    Call<List<Story>> getFollowingStories(@Header("Authorization") String authToken);

    @GET("stories/{id}/comments")
    Call<List<Comment>> getComments(@Path("id") int storyId);

    @POST("comments")
    Call<Comment> postComment(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    // ==================== LỊCH SỬ ĐỌC ====================
    @GET("reading-history")
    Call<List<ReadingHistoryItem>> getReadingHistory(@Header("Authorization") String authToken);

    @POST("reading-history/update")
    Call<Map<String, Object>> updateReadingHistory(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    // ==================== ĐÁNH GIÁ ====================
    @POST("ratings")
    Call<Map<String, Object>> postRating(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    // ==================== BỘ SƯU TẬP (Collections) ====================
    @GET("collections")
    Call<List<Collection>> getCollections(@Header("Authorization") String authToken);

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
}
