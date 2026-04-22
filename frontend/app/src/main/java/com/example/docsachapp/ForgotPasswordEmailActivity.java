package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.docsachapp.api.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordEmailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_email);

        ImageView btnBack = findViewById(R.id.btn_back);
        EditText etEmail = findViewById(R.id.et_email);
        TextView tvError = findViewById(R.id.tv_error);
        Button btnSend = findViewById(R.id.btn_send);

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            
            if (email.isEmpty()) {
                tvError.setText("Vui lòng nhập email của bạn");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            // Gọi API quên mật khẩu
            performForgotPassword(email, btnSend, tvError);
        });
    }

    private void performForgotPassword(String email, Button btnSend, TextView tvError) {
        btnSend.setEnabled(false);
        btnSend.setText("Đang xử lý...");
        tvError.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        RetrofitClient.getApi().forgotPassword(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                btnSend.setEnabled(true);
                btnSend.setText("Gửi");

                if (response.isSuccessful()) {
                    // Chuyển sang màn hình nhập OTP
                    Intent intent = new Intent(ForgotPasswordEmailActivity.this, ForgotPasswordOtpActivity.class);
                    intent.putExtra("email", email); // Gửi email sang màn hình tiếp theo
                    startActivity(intent);
                } else {
                    tvError.setText("Email không tồn tại trên hệ thống");
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnSend.setEnabled(true);
                btnSend.setText("Gửi");
                Toast.makeText(ForgotPasswordEmailActivity.this, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
