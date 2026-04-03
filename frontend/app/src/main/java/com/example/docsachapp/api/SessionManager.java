package com.example.docsachapp.api;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager.java
 * ====================
 * Lưu và đọc thông tin đăng nhập (token, user_id, username)
 * vào SharedPreferences — bộ nhớ nhỏ của Android, tồn tại khi tắt app.
 *
 * Dùng: Sau khi login thành công → lưu token.
 *       Mỗi lần gọi API cần auth → đọc token ra và gửi kèm.
 *
 * Token được gửi trong HTTP Header:
 *   Authorization: Token <token_value>
 */
public class SessionManager {

    // Tên file SharedPreferences
    private static final String PREF_NAME = "DocSachAppSession";

    // Các key để lưu dữ liệu
    private static final String KEY_TOKEN    = "auth_token";
    private static final String KEY_USER_ID  = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR   = "avatar_url";
    private static final String KEY_IS_LOGIN = "is_logged_in";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        // MODE_PRIVATE: chỉ app này đọc được (bảo mật)
        pref   = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Lưu thông tin sau khi đăng nhập thành công
     * @param token    Token nhận từ Django
     * @param userId   ID của NguoiDung profile
     * @param username Tên đăng nhập
     */
    public void saveLoginSession(String token, int userId, String username) {
        editor.putBoolean(KEY_IS_LOGIN, true);
        editor.putString(KEY_TOKEN,    token);
        editor.putInt(KEY_USER_ID,     userId);
        editor.putString(KEY_USERNAME, username);
        editor.apply(); // apply() không block UI, dùng thay cho commit()
    }

    /** Lưu thêm avatar URL (gọi sau khi load profile thành công) */
    public void saveAvatar(String avatarUrl) {
        editor.putString(KEY_AVATAR, avatarUrl);
        editor.apply();
    }

    /**
     * Lấy token để gửi kèm API request
     * Dùng: "Token " + sessionManager.getToken()
     */
    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    /**
     * Lấy header Authorization đầy đủ
     * Ví dụ: "Token abc123xyz"
     */
    public String getAuthHeader() {
        String token = getToken();
        if (token == null) return null;
        return "Token " + token;
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    /** Lấy avatar URL đã cache (có thể null) */
    public String getAvatar() {
        return pref.getString(KEY_AVATAR, null);
    }

    /**
     * Kiểm tra user đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGIN, false);
    }

    /**
     * Xóa session (đăng xuất)
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
