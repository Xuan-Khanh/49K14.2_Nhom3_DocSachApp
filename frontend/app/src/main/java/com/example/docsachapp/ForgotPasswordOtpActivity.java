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
import android.os.CountDownTimer;
import android.graphics.Color;
public class ForgotPasswordOtpActivity extends AppCompatActivity {
    private String email;
    private CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_otp);

        email = getIntent().getStringExtra("email");

        ImageView btnBack = findViewById(R.id.btn_back);
        EditText etOtp = findViewById(R.id.et_otp);
        TextView tvError = findViewById(R.id.tv_error);
        Button btnVerify = findViewById(R.id.btn_verify);
        ImageView ivRefresh = findViewById(R.id.iv_refresh); // thêm dòng này
        TextView tvTimer = findViewById(R.id.tv_timer_otp); // dùng biến class
        startOtpTimer(tvTimer);

        btnBack.setOnClickListener(v -> finish());

        // thêm click gửi lại OTP
        ivRefresh.setOnClickListener(v -> {
            Map<String, String> body = new HashMap<>();
            body.put("email", email);
            RetrofitClient.getApi().forgotPassword(body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ForgotPasswordOtpActivity.this, "Đã gửi lại mã OTP!", Toast.LENGTH_SHORT).show();
                        startOtpTimer(tvTimer);
                    } else {
                        Toast.makeText(ForgotPasswordOtpActivity.this, "Gửi lại thất bại!", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(ForgotPasswordOtpActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnVerify.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.isEmpty()) {
                tvError.setText("Vui lòng nhập mã OTP");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            verifyOtp(otp, btnVerify, tvError);
        });
    }
    private void verifyOtp(String otp, Button btnVerify, TextView tvError) {
        btnVerify.setEnabled(false);
        btnVerify.setText("Đang xác nhận...");
        tvError.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("otp", otp);

        RetrofitClient.getApi().verifyOtp(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Xác nhận");

                if (response.isSuccessful()) {
                    String resetToken = String.valueOf(response.body().get("reset_token"));
                    Intent intent = new Intent(ForgotPasswordOtpActivity.this, ForgotPasswordResetActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("reset_token", resetToken);
                    startActivity(intent);
                } else {
                    tvError.setText("Mã OTP không chính xác hoặc đã hết hạn");
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Xác nhận");
                Toast.makeText(ForgotPasswordOtpActivity.this, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void startOtpTimer(final TextView textViewTimer) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;

                String timeLeft = String.format("Mã hết hạn sau: %02d:%02d", minutes, seconds);
                textViewTimer.setText(timeLeft);
            }

            @Override
            public void onFinish() {
                textViewTimer.setText("Mã OTP đã hết hạn!");
                textViewTimer.setTextColor(Color.RED);
            }
        }.start();
    }
}
