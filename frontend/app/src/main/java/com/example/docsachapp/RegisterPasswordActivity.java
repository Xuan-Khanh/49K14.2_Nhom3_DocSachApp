package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.RegisterRequest;
import com.example.docsachapp.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Bước 2 đăng ký: Nhập và Xác nhận Mật khẩu, sau đó gọi API Đăng ký
public class RegisterPasswordActivity extends AppCompatActivity {
    private boolean isPassVis    = false;
    private boolean isConfirmVis = false;

    private String username;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_password);

        username = getIntent().getStringExtra("username");
        email    = getIntent().getStringExtra("email");

        if (username == null || email == null) {
            Toast.makeText(this, "Thiếu thông tin đăng ký", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy tham chiếu các View
        ImageView  btnBack          = findViewById(R.id.btn_back);
        EditText   etPass           = findViewById(R.id.et_password);
        EditText   etConfirm        = findViewById(R.id.et_confirm_password);
        ImageView  ivEyePass        = findViewById(R.id.iv_eye_pass);
        ImageView  ivEyeConfirm     = findViewById(R.id.iv_eye_confirm);
        TextView   tvError          = findViewById(R.id.tv_error);
        Button     btnRegister      = findViewById(R.id.btn_register);

        LinearLayout layoutRegisterForm = findViewById(R.id.layout_register_form);
        LinearLayout layoutSuccessPopup = findViewById(R.id.layout_success_popup);
        Button    btnBackToLogin        = findViewById(R.id.btn_back_to_login);

        btnBack.setOnClickListener(v -> finish());

        ivEyePass.setOnClickListener(v -> {
            isPassVis = !isPassVis;
            etPass.setInputType(isPassVis ?
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etPass.setSelection(etPass.getText().length());
        });

        ivEyeConfirm.setOnClickListener(v -> {
            isConfirmVis = !isConfirmVis;
            etConfirm.setInputType(isConfirmVis ?
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etConfirm.setSelection(etConfirm.getText().length());
        });

        btnRegister.setOnClickListener(v -> {
            String pass    = etPass.getText().toString();
            String confirm = etConfirm.getText().toString();

            if (pass.length() < 8) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText("Mật khẩu phải có ít nhất 8 ký tự");
                return;
            }
            if (!pass.equals(confirm)) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText("Mật khẩu xác nhận không khớp");
                return;
            }
            tvError.setVisibility(View.GONE);

            // GỌI API ĐĂNG KÝ TẠI ĐÂY NẾU HỢP LỆ
            callRegisterApi(pass, btnRegister, layoutRegisterForm, layoutSuccessPopup);
        });

        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void callRegisterApi(String password, Button btnRegister, LinearLayout layoutForm, LinearLayout layoutSuccess) {
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang xử lý...");

        RegisterRequest request = new RegisterRequest(username, email, password);

        // URL thực tế sẽ là BASE_URL + "auth/register"
        RetrofitClient.getApi().register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");

                if (response.isSuccessful()) {
                    // HIỆN POPUP THÀNH CÔNG NHƯ YÊU CẦU: Ẩn form đăng ký, hiện thông báo
                    layoutForm.setVisibility(View.GONE);
                    layoutSuccess.setVisibility(View.VISIBLE);
                } else {
                    String errorMsg = "Đăng ký không thành công";
                    if (response.code() == 400) {
                        errorMsg = "Tài khoản hoặc email đã tồn tại";
                    }
                    Toast.makeText(RegisterPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");
                Toast.makeText(RegisterPasswordActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
