package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;

/**
 * LoginResponse.java
 * ===================
 * Dữ liệu nhận về từ server sau khi đăng nhập thành công.
 * Django trả về JSON:
 * {
 *   "message": "Đăng nhập thành công.",
 *   "token": "abc123...",
 *   "user_id": 1,
 *   "username": "example",
 *   "avatar": "http://..." hoặc null
 * }
 *
 * @SerializedName: Map tên field JSON (Python snake_case) → tên biến Java
 */
public class LoginResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("token")
    private String token;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("username")
    private String username;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("error")
    private String error;   // Nếu đăng nhập thất bại

    // Getters
    public String getMessage() { return message; }
    public String getToken()   { return token; }
    public int    getUserId()  { return userId; }
    public String getUsername(){ return username; }
    public String getAvatar()  { return avatar; }
    public String getError()   { return error; }
}
