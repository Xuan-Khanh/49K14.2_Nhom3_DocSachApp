package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.LoginResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterMethodActivity extends AppCompatActivity {
    
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_method);

        sessionManager = new SessionManager(this);

        Button btnGoogle = findViewById(R.id.btn_google);
        Button btnFacebook = findViewById(R.id.btn_facebook);
        Button btnEmail = findViewById(R.id.btn_email);
        TextView tvLoginLink = findViewById(R.id.tv_login_link);

        // Chỉnh sửa: Gửi thông tin khớp với yêu cầu thông thường của Backend social login
        btnGoogle.setOnClickListener(v -> performSocialLogin("google", "google_test@gmail.com", "google_id_123"));
        
        btnFacebook.setOnClickListener(v -> performSocialLogin("facebook", "fb_test@gmail.com", "fb_id_123"));

        btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterMethodActivity.this, RegisterInfoActivity.class);
            startActivity(intent);
        });

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterMethodActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void performSocialLogin(String provider, String email, String socialId) {
        Map<String, String> body = new HashMap<>();
        body.put("provider", provider);
        body.put("email", email);
        body.put("social_id", socialId); // Thêm social_id nếu backend cần
        body.put("access_token", "mock_token_valid_" + provider); // Đổi sang access_token thường dùng

        RetrofitClient.getApi().socialLogin(body).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    sessionManager.saveLoginSession(
                            loginResponse.getToken(),
                            loginResponse.getUserId(),
                            loginResponse.getUsername()
                    );
                    startActivity(new Intent(RegisterMethodActivity.this, MainActivity.class));
                    finishAffinity();
                } else {
                    // HIỂN THỊ LỖI CHI TIẾT TỪ SERVER
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown Error";
                        Log.e("SocialLogin", "Error: " + errorBody);
                        Toast.makeText(RegisterMethodActivity.this, "Server báo lỗi: " + response.code(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(RegisterMethodActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("SocialLogin", "Failure: " + t.getMessage());
                Toast.makeText(RegisterMethodActivity.this, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
