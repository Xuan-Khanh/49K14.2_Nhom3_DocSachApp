package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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

// Màn hình Bước 3 Quên mật khẩu: Nhập và Xác nhận Mật khẩu mới
public class ForgotPasswordResetActivity extends AppCompatActivity {
    private boolean isPassVis = false;
    private boolean isConfirmVis = false;
    private String email, resetToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_reset);

        email = getIntent().getStringExtra("email");
        resetToken = getIntent().getStringExtra("reset_token");  // đổi "otp" -> "reset_token"

        ImageView btnBack = findViewById(R.id.btn_back);
        EditText etPass = findViewById(R.id.et_new_password);
        EditText etConfirm = findViewById(R.id.et_confirm_password);
        ImageView ivEyePass = findViewById(R.id.iv_eye_pass);
        ImageView ivEyeConfirm = findViewById(R.id.iv_eye_confirm);
        TextView tvError = findViewById(R.id.tv_error);
        Button btnUpdate = findViewById(R.id.btn_update);

        btnBack.setOnClickListener(v -> finish());

        // Xử lý nút "Con Mắt" để Hiện / Ẩn mật khẩu (cho ô Mật khẩu mới)
        ivEyePass.setOnClickListener(v -> {
            isPassVis = !isPassVis;
            // Dùng InputType để đổi kiểu hiển thị: TEXT (Hiện) <-> PASSWORD (Dấu chấm)
            etPass.setInputType(isPassVis ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            // Đưa con trỏ nháy về cuối chuỗi sau khi đổi trạng thái
            etPass.setSelection(etPass.getText().length());
        });

        ivEyeConfirm.setOnClickListener(v -> {
            isConfirmVis = !isConfirmVis;
            etConfirm.setInputType(isConfirmVis ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etConfirm.setSelection(etConfirm.getText().length());
        });

        btnUpdate.setOnClickListener(v -> {
            String newPassword = etPass.getText().toString();
            String confirmPassword = etConfirm.getText().toString();

            // Kiểm tra bảo mật cơ bản: Phải dài hơn 8 ký tự
            if (newPassword.length() < 8) {
                tvError.setText("Mật khẩu mới phải có ít nhất 8 ký tự");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            // Kiểm tra khớp mật khẩu
            if (!newPassword.equals(confirmPassword)) {
                tvError.setText("Mật khẩu xác nhận không khớp");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            // Đủ điều kiện -> Gọi API
            resetPassword(newPassword, btnUpdate, tvError);
        });
    }

    private void resetPassword(String newPassword, Button btnUpdate, TextView tvError) {
        btnUpdate.setEnabled(false);
        btnUpdate.setText("Đang cập nhật...");
        tvError.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("reset_token", resetToken);  // đổi otp -> reset_token
        body.put("new_password", newPassword);

        RetrofitClient.getApi().resetPassword(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                btnUpdate.setEnabled(true);
                btnUpdate.setText("Cập nhật");

                if (response.isSuccessful()) {
                    // THÀNH CÔNG: Chuyển sang màn hình Thông báo thành công (Bước 4)
                    Intent intent = new Intent(ForgotPasswordResetActivity.this, ForgotPasswordSuccessActivity.class);
                    // FLAG_ACTIVITY_CLEAR_TASK giúp xóa sạch các màn hình trước (Bước 1, 2, 3) 
                    // để người dùng không thể bấm Back quay lại màn đổi pass nữa
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(ForgotPasswordResetActivity.this, "Không thể đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnUpdate.setEnabled(true);
                btnUpdate.setText("Cập nhật");
                Toast.makeText(ForgotPasswordResetActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
