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
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.SerializedName;

import com.google.gson.annotations.SerializedName;

import com.google.gson.annotations.SerializedName;
import com.example.docsachapp.model.UserFollowItem;
public interface ApiService {

    // ==================== AUTH & PROFILE ====================

    @POST("auth/login/")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register/")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    /** API Quên mật khẩu - Gửi email nhận OTP */
    @POST("auth/forgot-password/")
    Call<Map<String, Object>> forgotPassword(@Body Map<String, String> body);

    /** API Xác nhận mã OTP */
    @POST("auth/verify-otp/")
    Call<Map<String, Object>> verifyOtp(@Body Map<String, String> body);

    /** API Đặt lại mật khẩu mới */
    @POST("auth/reset-password/")
    Call<Map<String, Object>> resetPassword(@Body Map<String, String> body);

    /** API Đăng nhập bằng mạng xã hội (Google/Facebook) */
    @POST("auth/social-login/")
    Call<LoginResponse> socialLogin(@Body Map<String, String> body);

    @GET("auth/profile/")
    Call<UserProfile> getUserProfile(@Header("Authorization") String authToken);

    @PUT("auth/profile/")
    Call<Map<String, Object>> updateUserProfile(@Header("Authorization") String authToken, @Body Map<String, String> body);

    @GET("users/{id}/")
    Call<UserProfile> getPublicProfile(@Path("id") int userId);

    // ==================== TRUYỆN ====================

    @GET("stories/")
    Call<List<Story>> getStories(
            @Query("search") String search,
            @Query("theloai") Integer theLoaiId,
            @Query("trang_thai") String trangThai
    );

    @GET("stories/new-releases/")
    Call<List<Story>> getNewReleases();

    @GET("stories/recently-updated/")
    Call<List<Story>> getRecentlyUpdated();

    @GET("stories/completed/")
    Call<List<Story>> getCompletedStories();

    @GET("stories/")
    Call<List<Story>> searchStories(@Query("search") String keyword);

    @GET("stories/{id}/")
    Call<Story> getStoryDetail(@Path("id") int id);

    @GET("stories/{id}/chapters/")
    Call<List<Chapter>> getChapters(@Path("id") int storyId);

    @GET("chapters/{id}/")
    Call<Chapter> getChapterDetail(@Path("id") int chapterId);

    // ==================== THEO DÕI ====================

    @GET("user/following-stories/")
    Call<List<Story>> getFollowingStories(@Header("Authorization") String authToken);

    @POST("user/follow-story/")
    Call<Map<String, Object>> followStory(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    @POST("user/unfollow-story/")
    Call<Map<String, Object>> unfollowStory(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    // ==================== LỊCH SỬ ĐỌC ====================

    @GET("reading-history/")
    Call<List<ReadingHistoryItem>> getReadingHistory(@Header("Authorization") String authToken);

    @POST("reading-history/update/")
    Call<Map<String, Object>> updateReadingHistory(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    // ==================== BÌNH LUẬN ====================

    @GET("stories/{id}/comments/")
    Call<List<Comment>> getComments(@Path("id") int storyId);

    @POST("comments/")
    Call<Comment> postComment(@Header("Authorization") String authToken, @Body Map<String, Object> body);

    // ==================== BỘ SƯU TẬP ====================

    @GET("collections/")
    Call<Map<String, List<Collection>>> getCollections(@Header("Authorization") String authToken);

    // ==================== NGƯỜI THEO DÕI - ĐANG ====================

    @GET("auth/followers/")
    Call<List<UserFollowItem>> getFollowers(@Header("Authorization") String token);

    @GET("auth/following/")
    Call<List<UserFollowItem>> getFollowing(@Header("Authorization") String token);
}
