package com.example.docsachapp.model;

/**
 * LoginRequest.java
 * ==================
 * Dữ liệu gửi lên server khi đăng nhập.
 * Gson sẽ tự chuyển object này thành JSON:
 * {
 *   "username": "...",
 *   "password": "..."
 * }
 */
public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters (Gson cần để serialize)
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
