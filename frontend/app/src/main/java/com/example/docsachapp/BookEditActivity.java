package com.example.docsachapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Chapter;
import com.example.docsachapp.model.Story;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookEditActivity extends AppCompatActivity {

    private View layoutMain, popupTitle, popupDesc, popupCategory, popupManageChapters;
    private TextView tvDisplayTitle, tvDisplayDesc, tvDisplayCategory, tvStatusLabel;
    private EditText etEditTitle, etEditDesc;
    private Switch switchStatus;
    private ImageView ivBookCover, btnSave;
    private LinearLayout containerChaptersList, containerManageChaptersList;
    private TextView btnDeleteSelectedChapters, btnStatusSelectedChapters;

    private String originalTitle, originalDesc, originalStatus;
    private List<Story.Genre> allGenresFromServer = new ArrayList<>();
    private final Set<Integer> originalGenreIds = new HashSet<>();
    private Set<Integer> currentSelectedGenreIds = new HashSet<>();

    private List<Chapter> currentChaptersList = new ArrayList<>();
    private final Set<Integer> selectedChapters = new HashSet<>();

    private int storyId = -1;
    private SessionManager sessionManager;
    private boolean isDataLoaded = false;
    private Uri currentPhotoUri;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    currentPhotoUri = result.getData().getData();
                    ivBookCover.setImageURI(currentPhotoUri);
                    checkChanges();
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    ivBookCover.setImageURI(currentPhotoUri);
                    checkChanges();
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openCamera();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_edit);

        sessionManager = new SessionManager(this);
        storyId = getIntent().getIntExtra("STORY_ID", -1);

        layoutMain = findViewById(R.id.layout_edit_main);
        popupTitle = findViewById(R.id.popup_edit_title);
        popupDesc = findViewById(R.id.popup_edit_desc);
        popupCategory = findViewById(R.id.popup_choose_category);
        ivBookCover = findViewById(R.id.iv_book_cover);
        btnSave = findViewById(R.id.btn_save);
        tvDisplayTitle = findViewById(R.id.tv_display_title);
        tvDisplayDesc = findViewById(R.id.tv_book_desc);
        tvDisplayCategory = findViewById(R.id.tv_book_categories);
        tvStatusLabel = findViewById(R.id.tv_status_label);
        switchStatus = findViewById(R.id.switch_status);
        etEditTitle = findViewById(R.id.et_edit_title);
        etEditDesc = findViewById(R.id.et_edit_desc);
        containerChaptersList = findViewById(R.id.container_chapters_list);
        
        popupManageChapters = findViewById(R.id.popup_manage_chapters);
        containerManageChaptersList = findViewById(R.id.container_manage_chapters_list);
        btnDeleteSelectedChapters = findViewById(R.id.btn_delete_selected_chapters);
        btnStatusSelectedChapters = findViewById(R.id.btn_status_selected_chapters);

        disableSaveButton();

        if (storyId != -1) {
            loadStoryDetail();
            loadAllGenres();
            loadChapters();
        }

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (storyId != -1) {
            loadChapters();
        }
    }

    private void setupListeners() {
        TextWatcher changeWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { checkChanges(); }
        };
        etEditTitle.addTextChangedListener(changeWatcher);
        etEditDesc.addTextChangedListener(changeWatcher);

        findViewById(R.id.layout_edit_cover).setOnClickListener(v -> showImagePickerDialog());

        findViewById(R.id.layout_edit_title).setOnClickListener(v -> {
            etEditTitle.setText(tvDisplayTitle.getText().toString());
            showPopup(popupTitle);
        });

        findViewById(R.id.layout_edit_desc).setOnClickListener(v -> {
            etEditDesc.setText(tvDisplayDesc.getText().toString());
            showPopup(popupDesc);
        });

        findViewById(R.id.layout_edit_category).setOnClickListener(v -> {
            setupCategoryListView();
            showPopup(popupCategory);
        });

        findViewById(R.id.btn_back_title).setOnClickListener(v -> {
            tvDisplayTitle.setText(etEditTitle.getText().toString());
            checkChanges();
            hidePopups();
        });
        findViewById(R.id.btn_back_desc).setOnClickListener(v -> {
            tvDisplayDesc.setText(etEditDesc.getText().toString());
            checkChanges();
            hidePopups();
        });
        findViewById(R.id.btn_back_category).setOnClickListener(v -> {
            updateCategoryUI();
            checkChanges();
            hidePopups();
        });

        switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvStatusLabel.setText(isChecked ? "Đã hoàn thành" : "Đang tiến hành");
            tvStatusLabel.setTextColor(isChecked ? Color.parseColor("#4CAF50") : Color.parseColor("#888888"));
            checkChanges();
        });

        btnSave.setOnClickListener(v -> saveChangesToServer());
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_delete_book).setOnClickListener(v -> showDeleteConfirmation());

        // Thêm chương mới
        View btnAddChapter = findViewById(R.id.btn_add_chapter);
        if (btnAddChapter != null) {
            btnAddChapter.setOnClickListener(v -> {
                Intent intent = new Intent(BookEditActivity.this, ChapterWriterActivity.class);
                intent.putExtra("STORY_ID", storyId);
                startActivity(intent);
            });
        }

        // Mở cài đặt (Quản lý chương / Xóa hàng loạt)
        View btnSettings = findViewById(R.id.btn_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                selectedChapters.clear();
                renderManageChaptersList();
                showPopup(popupManageChapters);
            });
        }

        findViewById(R.id.btn_close_manage_chapters).setOnClickListener(v -> hidePopups());

        btnDeleteSelectedChapters.setOnClickListener(v -> {
            if (selectedChapters.isEmpty()) return;
            new AlertDialog.Builder(this)
                .setTitle("Xóa chương")
                .setMessage("Bạn có chắc chắn muốn xóa " + selectedChapters.size() + " chương đã chọn?")
                .setPositiveButton("Xóa", (dialog, which) -> performBatchAction("delete"))
                .setNegativeButton("Hủy", null)
                .show();
        });

        btnStatusSelectedChapters.setOnClickListener(v -> {
            if (selectedChapters.isEmpty()) return;
            String text = btnStatusSelectedChapters.getText().toString();
            String action = text.equals("ĐĂNG") ? "publish" : "unpublish";
            String title = text.equals("ĐĂNG") ? "Đăng chương" : "Ngừng đăng chương";
            
            new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("Bạn muốn " + text.toLowerCase() + " " + selectedChapters.size() + " chương đã chọn?")
                .setPositiveButton("Đồng ý", (dialog, which) -> performBatchAction(action))
                .setNegativeButton("Hủy", null)
                .show();
        });
    }

    private void performBatchAction(String action) {
        if (selectedChapters.isEmpty()) return;
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("chapter_ids", new ArrayList<>(selectedChapters));
        body.put("action", action);
        
        String token = sessionManager.getAuthHeader();
        RetrofitClient.getApi().batchActionChapters(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookEditActivity.this, "Thao tác thành công", Toast.LENGTH_SHORT).show();
                    selectedChapters.clear();
                    hidePopups();
                    loadChapters();
                } else {
                    Toast.makeText(BookEditActivity.this, "Lỗi từ máy chủ: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Toast.makeText(BookEditActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new AlertDialog.Builder(this)
                .setTitle("Thay đổi ảnh bìa")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            openCamera();
                        } else {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                        }
                    } else {
                        galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                    }
                })
                .show();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewCover");
        currentPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
        cameraLauncher.launch(intent);
    }

    private void loadStoryDetail() {
        String token = sessionManager.getAuthHeader();
        RetrofitClient.getApi().getStoryDetail(token, storyId).enqueue(new Callback<Story>() {
            @Override
            public void onResponse(@NonNull Call<Story> call, @NonNull Response<Story> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Story story = response.body();
                    originalTitle = story.getTitle();
                    originalDesc = story.getDescription();
                    originalStatus = story.getStatus();
                    tvDisplayTitle.setText(originalTitle);
                    tvDisplayDesc.setText(originalDesc);
                    originalGenreIds.clear();
                    if (story.getGenres() != null) {
                        for (Story.Genre g : story.getGenres()) originalGenreIds.add(g.getId());
                    }
                    currentSelectedGenreIds = new HashSet<>(originalGenreIds);
                    updateCategoryUI();
                    switchStatus.setChecked("hoan_thanh".equals(originalStatus));
                    if (!isDestroyed()) Glide.with(BookEditActivity.this).load(story.getCoverUrl()).placeholder(R.drawable.anhtruyen).into(ivBookCover);
                    isDataLoaded = true;
                    disableSaveButton();
                } else {
                    Toast.makeText(BookEditActivity.this, "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<Story> call, @NonNull Throwable t) {
                Toast.makeText(BookEditActivity.this, "Kết nối Server thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllGenres() {
        RetrofitClient.getApi().getGenres().enqueue(new Callback<List<Story.Genre>>() {
            @Override
            public void onResponse(@NonNull Call<List<Story.Genre>> call, @NonNull Response<List<Story.Genre>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allGenresFromServer = response.body();
                    updateCategoryUI();
                }
            }
            @Override public void onFailure(@NonNull Call<List<Story.Genre>> call, @NonNull Throwable t) {}
        });
    }

    private void loadChapters() {
        String token = sessionManager.getAuthHeader();
        RetrofitClient.getApi().getChapters(token, storyId).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(@NonNull Call<List<Chapter>> call, @NonNull Response<List<Chapter>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentChaptersList = response.body();
                    renderChaptersList();
                }
            }
            @Override public void onFailure(@NonNull Call<List<Chapter>> call, @NonNull Throwable t) {}
        });
    }

    private String formatStatText(int number) {
        if (number >= 1000000) {
            return String.format(java.util.Locale.US, "%.1f M", number / 1000000.0).replace(".0 M", " M");
        } else if (number >= 1000) {
            return String.format(java.util.Locale.US, "%.1f N", number / 1000.0).replace(".0 N", " N").replace(".", ",");
        }
        return String.valueOf(number);
    }

    private void renderChaptersList() {
        if (containerChaptersList == null) return;
        containerChaptersList.removeAllViews();
        for (Chapter chapter : currentChaptersList) {
            View itemView = getLayoutInflater().inflate(R.layout.item_chapter_edit, containerChaptersList, false);
            
            TextView tvTitle = itemView.findViewById(R.id.tv_chapter_title);
            TextView tvBadge = itemView.findViewById(R.id.tv_status_badge);
            TextView tvDate = itemView.findViewById(R.id.tv_chapter_date);
            ImageView ivCheckbox = itemView.findViewById(R.id.iv_checkbox);
            ImageView ivChevron = itemView.findViewById(R.id.iv_chevron);
            
            TextView tvViews = itemView.findViewById(R.id.tv_chapter_views);
            TextView tvBookmarks = itemView.findViewById(R.id.tv_chapter_bookmarks);
            TextView tvComments = itemView.findViewById(R.id.tv_chapter_comments);
            
            tvTitle.setText(chapter.getTitle());
            if (chapter.isPublished()) {
                tvBadge.setText("Đã đăng tải");
                tvBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8C88A")));
            } else {
                tvBadge.setText("Bản thảo");
                tvBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#D9A441")));
            }
            tvDate.setText(chapter.getCreatedAt() != null ? chapter.getCreatedAt().substring(0, 10) : "");
            
            if (tvViews != null) tvViews.setText(formatStatText(chapter.getViews()));
            if (tvBookmarks != null) tvBookmarks.setText(formatStatText(chapter.getBookmarks()));
            if (tvComments != null) tvComments.setText(formatStatText(chapter.getComments()));
            
            ivCheckbox.setVisibility(View.GONE);
            ivChevron.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(BookEditActivity.this, ChapterWriterActivity.class);
                intent.putExtra("STORY_ID", storyId);
                intent.putExtra("CHAPTER_ID", chapter.getId());
                startActivity(intent);
            });
            
            containerChaptersList.addView(itemView);
        }
    }

    private void renderManageChaptersList() {
        if (containerManageChaptersList == null) return;
        containerManageChaptersList.removeAllViews();
        for (Chapter chapter : currentChaptersList) {
            View itemView = getLayoutInflater().inflate(R.layout.item_chapter_edit, containerManageChaptersList, false);
            
            TextView tvTitle = itemView.findViewById(R.id.tv_chapter_title);
            TextView tvBadge = itemView.findViewById(R.id.tv_status_badge);
            TextView tvDate = itemView.findViewById(R.id.tv_chapter_date);
            ImageView ivCheckbox = itemView.findViewById(R.id.iv_checkbox);
            ImageView ivChevron = itemView.findViewById(R.id.iv_chevron);
            
            TextView tvViews = itemView.findViewById(R.id.tv_chapter_views);
            TextView tvBookmarks = itemView.findViewById(R.id.tv_chapter_bookmarks);
            TextView tvComments = itemView.findViewById(R.id.tv_chapter_comments);
            
            tvTitle.setText(chapter.getTitle());
            if (chapter.isPublished()) {
                tvBadge.setText("Đã đăng tải");
                tvBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8C88A")));
            } else {
                tvBadge.setText("Bản thảo");
                tvBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#D9A441")));
            }
            tvDate.setText(chapter.getCreatedAt() != null ? chapter.getCreatedAt().substring(0, 10) : "");
            
            if (tvViews != null) tvViews.setText(formatStatText(chapter.getViews()));
            if (tvBookmarks != null) tvBookmarks.setText(formatStatText(chapter.getBookmarks()));
            if (tvComments != null) tvComments.setText(formatStatText(chapter.getComments()));
            
            ivChevron.setVisibility(View.GONE);
            ivCheckbox.setVisibility(View.VISIBLE);
            ivCheckbox.setImageResource(selectedChapters.contains(chapter.getId()) ? R.drawable.ic_circle_filled : R.drawable.ic_circle_outline);

            itemView.setOnClickListener(v -> {
                if (selectedChapters.contains(chapter.getId())) {
                    selectedChapters.remove(chapter.getId());
                } else {
                    selectedChapters.add(chapter.getId());
                }
                ivCheckbox.setImageResource(selectedChapters.contains(chapter.getId()) ? R.drawable.ic_circle_filled : R.drawable.ic_circle_outline);
                updateManageBottomBar();
            });
            
            containerManageChaptersList.addView(itemView);
        }
        updateManageBottomBar();
    }

    private void updateManageBottomBar() {
        if (selectedChapters.isEmpty()) {
            btnDeleteSelectedChapters.setAlpha(0.3f);
            btnStatusSelectedChapters.setAlpha(0.3f);
            btnStatusSelectedChapters.setText("ĐĂNG / NGỪNG ĐĂNG");
            return;
        }
        btnDeleteSelectedChapters.setAlpha(1.0f);
        btnStatusSelectedChapters.setAlpha(1.0f);
        
        boolean hasPublished = false;
        boolean hasDraft = false;
        for (Chapter c : currentChaptersList) {
            if (selectedChapters.contains(c.getId())) {
                if (c.isPublished()) hasPublished = true;
                else hasDraft = true;
            }
        }
        
        if (hasPublished && !hasDraft) {
            btnStatusSelectedChapters.setText("NGỪNG ĐĂNG");
        } else if (hasDraft && !hasPublished) {
            btnStatusSelectedChapters.setText("ĐĂNG");
        } else {
            btnStatusSelectedChapters.setText("ĐỔI TRẠNG THÁI");
        }
    }

    private void setupCategoryListView() {
        LinearLayout container = findViewById(R.id.container_categories);
        container.removeAllViews();
        for (Story.Genre genre : allGenresFromServer) {
            View row = getLayoutInflater().inflate(R.layout.item_category_select, container, false);
            TextView tv = row.findViewById(R.id.tv_category_name);
            ImageView ivCheck = row.findViewById(R.id.iv_check);
            tv.setText(genre.getName());
            ivCheck.setVisibility(currentSelectedGenreIds.contains(genre.getId()) ? View.VISIBLE : View.GONE);
            row.setOnClickListener(v -> {
                if (currentSelectedGenreIds.contains(genre.getId())) {
                    currentSelectedGenreIds.remove(genre.getId());
                    ivCheck.setVisibility(View.GONE);
                } else {
                    currentSelectedGenreIds.add(genre.getId());
                    ivCheck.setVisibility(View.VISIBLE);
                }
            });
            container.addView(row);
        }
    }

    private void updateCategoryUI() {
        if (currentSelectedGenreIds.isEmpty()) tvDisplayCategory.setText("Chưa chọn");
        else {
            List<String> names = new ArrayList<>();
            for (Story.Genre g : allGenresFromServer) if (currentSelectedGenreIds.contains(g.getId())) names.add(g.getName());
            if (!names.isEmpty()) tvDisplayCategory.setText(String.join(", ", names));
        }
    }

    private void checkChanges() {
        if (!isDataLoaded) return;
        boolean changed = !tvDisplayTitle.getText().toString().equals(originalTitle) ||
                !tvDisplayDesc.getText().toString().equals(originalDesc) ||
                !(switchStatus.isChecked() ? "hoan_thanh" : "da_dang").equals(originalStatus) ||
                !currentSelectedGenreIds.equals(originalGenreIds) || (currentPhotoUri != null);
        if (changed) enableSaveButton(); else disableSaveButton();
    }

    private void enableSaveButton() { btnSave.setEnabled(true); btnSave.setAlpha(1.0f); btnSave.setColorFilter(Color.parseColor("#D9A441")); }
    private void disableSaveButton() { btnSave.setEnabled(false); btnSave.setAlpha(0.3f); btnSave.setColorFilter(Color.GRAY); }

    private void saveChangesToServer() {
        String token = sessionManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Bạn cần đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển dữ liệu sang chuẩn RequestBody cho Multipart
        RequestBody titlePart = RequestBody.create(tvDisplayTitle.getText().toString(), MediaType.parse("text/plain"));
        RequestBody descPart = RequestBody.create(tvDisplayDesc.getText().toString(), MediaType.parse("text/plain"));
        RequestBody statusPart = RequestBody.create(switchStatus.isChecked() ? "hoan_thanh" : "da_dang", MediaType.parse("text/plain"));

        List<MultipartBody.Part> genreParts = new ArrayList<>();
        for (Integer id : currentSelectedGenreIds) {
            genreParts.add(MultipartBody.Part.createFormData("the_loai", String.valueOf(id)));
        }

        MultipartBody.Part bodyCover = null;
        if (currentPhotoUri != null) {
            try {
                File file = uriToFile(currentPhotoUri);
                RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
                bodyCover = MultipartBody.Part.createFormData("anh_bia", file.getName(), requestFile);
            } catch (Exception e) { e.printStackTrace(); }
        }

        Toast.makeText(this, "Đang lưu...", Toast.LENGTH_SHORT).show();
        RetrofitClient.getApi().updateStoryMultipart(token, storyId, titlePart, descPart, statusPart, genreParts, bodyCover).enqueue(new Callback<Story>() {
            @Override
            public void onResponse(@NonNull Call<Story> call, @NonNull Response<Story> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookEditActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("API_ERROR", "Code: " + response.code() + " | Body: " + errorMsg);
                        Toast.makeText(BookEditActivity.this, "Lỗi Server (" + response.code() + ")", Toast.LENGTH_LONG).show();
                    } catch (IOException e) { e.printStackTrace(); }
                }
            }
            @Override public void onFailure(@NonNull Call<Story> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failure: " + t.getMessage());
                Toast.makeText(BookEditActivity.this, "Kết nối thất bại. Kiểm tra IP máy tính!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa truyện")
                .setMessage("Bạn có chắc chắn muốn xóa truyện này?")
                .setPositiveButton("XÓA", (dialog, which) -> deleteStory())
                .setNegativeButton("HỦY", null).show();
    }

    private void deleteStory() {
        String token = sessionManager.getAuthHeader();
        RetrofitClient.getApi().deleteStory(token, storyId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookEditActivity.this, "Đã xóa truyện", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(BookEditActivity.this, "Lỗi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                 Toast.makeText(BookEditActivity.this, "Kết nối thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File uriToFile(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = new File(getCacheDir(), "temp_edit_cover.jpg");
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, bytesRead);
        outputStream.close(); inputStream.close();
        return tempFile;
    }

    private void showPopup(View p) { layoutMain.setVisibility(View.GONE); p.setVisibility(View.VISIBLE); }
    private void hidePopups() { popupTitle.setVisibility(View.GONE); popupDesc.setVisibility(View.GONE); popupCategory.setVisibility(View.GONE); popupManageChapters.setVisibility(View.GONE); layoutMain.setVisibility(View.VISIBLE); }
}
