package com.example.docsachapp.model;

/**
 * RegisterRequest.java
 * =====================
 * Dữ liệu gửi lên khi đăng ký tài khoản.
 * JSON gửi đi:
 * {
 *   "username": "...",
 *   "email": "...",
 *   "password": "..."
 * }
 */
public class RegisterRequest {
    private String username;
    private String email;
    private String password;

    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email    = email;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }
}
