package com.example.docsachapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.UserProfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ProfileEditActivity – Chỉnh sửa hồ sơ người dùng
 * ✅ FIX #2 & #8: Hỗ trợ chọn avatar từ Camera/Thư viện, gửi multipart/form-data
 */
public class ProfileEditActivity extends AppCompatActivity {

    private EditText etUsername, etBio, etBirthday, etEmail;
    private Button btnSave;
    private ImageView btnBack, ivAvatar;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    private UserProfile currentProfile;
    private Uri selectedAvatarUri;

    // ✅ FIX #2: Launcher cho thư viện ảnh
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedAvatarUri = result.getData().getData();
                    if (ivAvatar != null) {
                        Glide.with(this).load(selectedAvatarUri)
                                .circleCrop()
                                .into(ivAvatar);
                    }
                }
            });

    // ✅ FIX #2: Launcher cho camera
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (ivAvatar != null && selectedAvatarUri != null) {
                        Glide.with(this).load(selectedAvatarUri)
                                .circleCrop()
                                .into(ivAvatar);
                    }
                }
            });

    // ✅ FIX #2: Xin quyền camera
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openCamera();
                else Toast.makeText(this, "Cần quyền Camera để chụp ảnh", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        sessionManager = new SessionManager(this);
        String token = sessionManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadProfile(token);
    }

    private void initViews() {
        btnBack     = findViewById(R.id.btn_back);
        btnSave     = findViewById(R.id.btn_save);
        etUsername  = findViewById(R.id.et_username);
        etBio       = findViewById(R.id.et_bio);
        etBirthday  = findViewById(R.id.et_birthday);
        etEmail     = findViewById(R.id.et_email);
        ivAvatar    = findViewById(R.id.iv_avatar);
        progressBar = findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> finish());

        // ✅ FIX #2: Click vào avatar → hiện dialog chọn Camera hoặc Thư viện
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> showImagePickerDialog());
        }

        btnSave.setOnClickListener(v -> {
            String username = etUsername != null ? etUsername.getText().toString().trim() : "";
            String bio      = etBio      != null ? etBio.getText().toString().trim() : "";

            if (username.isEmpty()) {
                if (etUsername != null) etUsername.setError("Không được để trống");
                return;
            }
            saveProfile(username, bio);
        });
    }

    /** ✅ FIX #2: Dialog cho phép chọn từ Camera hoặc Thư viện */
    private void showImagePickerDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new AlertDialog.Builder(this)
                .setTitle("Thay đổi ảnh đại diện")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            openCamera();
                        } else {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                        }
                    } else {
                        galleryLauncher.launch(new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                    }
                })
                .show();
    }

    /** ✅ FIX #2: Mở camera chụp ảnh */
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewAvatar");
        selectedAvatarUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedAvatarUri);
        cameraLauncher.launch(intent);
    }

    private void loadProfile(String token) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getApi().getUserProfile(token).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(@NonNull Call<UserProfile> call, @NonNull Response<UserProfile> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    fillForm(currentProfile);
                } else {
                    Toast.makeText(ProfileEditActivity.this, "Không thể tải hồ sơ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileEditActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillForm(UserProfile profile) {
        if (etUsername != null) etUsername.setText(profile.getUsername());
        if (etBio != null)      etBio.setText(profile.getBio());
        if (etBirthday != null) etBirthday.setText(profile.getBirthday() != null ? profile.getBirthday() : "");
        if (etEmail != null)    etEmail.setText(profile.getEmail());
        
        if (ivAvatar != null && profile.getAvatar() != null) {
            Glide.with(this).load(profile.getAvatar())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .circleCrop()
                    .into(ivAvatar);
        }
    }

    /** ✅ FIX #6: Gửi multipart/form-data đầy đủ fields: username, mo_ta, email, ngay_sinh */
    private void saveProfile(String username, String bio) {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Tạo Map RequestBody cho các trường văn bản (Bắt buộc phải dùng Multipart PartMap vì có kèm File ảnh)
        Map<String, RequestBody> parts = new HashMap<>();
        parts.put("username", RequestBody.create(username, MediaType.parse("text/plain")));
        parts.put("mo_ta", RequestBody.create(bio, MediaType.parse("text/plain")));

        // ✅ FIX: Gửi kèm email nếu có
        if (etEmail != null) {
            String email = etEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                parts.put("email", RequestBody.create(email, MediaType.parse("text/plain")));
            }
        }

        // ✅ FIX: Gửi kèm ngay_sinh nếu có
        if (etBirthday != null) {
            String birthday = etBirthday.getText().toString().trim();
            if (!birthday.isEmpty()) {
                parts.put("ngay_sinh", RequestBody.create(birthday, MediaType.parse("text/plain")));
            }
        }

        // ✅ FIX #8: Nếu đã chọn avatar mới → convert sang MultipartBody.Part
        MultipartBody.Part avatarPart = null;
        if (selectedAvatarUri != null) {
            try {
                File file = uriToFile(selectedAvatarUri);
                RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
                avatarPart = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
            }
        }

        // Gọi API với 3 tham số: token, PartMap (chứa các chuỗi text), và avatarPart (chứa file ảnh)
        RetrofitClient.getApi().updateUserProfile(token, parts, avatarPart).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(ProfileEditActivity.this, "Đã lưu hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ProfileEditActivity.this, "Lưu thất bại (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(ProfileEditActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Convert Uri thành File tạm */
    private File uriToFile(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = new File(getCacheDir(), "temp_avatar.jpg");
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
        return tempFile;
    }
}
