package com.example.docsachapp.model;

import com.google.gson.annotations.SerializedName;

/**
 * RegisterResponse.java
 * ======================
 * Dữ liệu nhận về sau khi đăng ký thành công.
 * Django trả về:
 * {
 *   "message": "Đăng ký thành công.",
 *   "token": "abc123...",
 *   "user_id": 5,
 *   "username": "newuser"
 * }
 */
public class RegisterResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("token")
    private String token;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("username")
    private String username;

    @SerializedName("error")
    private String error;

    public String getMessage() { return message; }
    public String getToken()   { return token; }
    public int    getUserId()  { return userId; }
    public String getUsername(){ return username; }
    public String getError()   { return error; }
}
