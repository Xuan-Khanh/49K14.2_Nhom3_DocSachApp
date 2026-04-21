package com.example.docsachapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.Story;
import com.example.docsachapp.model.UserProfile;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class TuSachFragment extends Fragment {

    private View layoutMain, popupAuthorWorks, popupCreateStep1, popupCreateStep2, popupChooseCategory;
    private SessionManager sessionManager;

    private RoundedImageView ivRecentCover;
    private TextView tvRecentTitle, tvRecentStatus;
    private View btnEditRecent;
    private LinearLayout containerMyWorks;

    // Bước 1
    private EditText etBookTitle, etBookDesc;
    private FrameLayout btnAddCover;
    private ImageView btnNextStep1, ivCoverPreview, ivAddIcon;
    private Uri currentPhotoUri;

    // Bước 2
    private TextView tvStep2Title, tvStep2Desc, tvStep2Category, tvStep2StatusLabel;
    private Switch switchStatusCreate;
    private ImageView btnDoneCreate;
    private View popupEditTitleStep2, popupEditDescStep2;
    private EditText etEditTitleStep2, etEditDescStep2;
    private ImageView ivBookCoverStep2;

    // Thể loại
    private List<Story.Genre> allGenres = new ArrayList<>();
    private Set<Integer> selectedGenreIds = new HashSet<>();

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    currentPhotoUri = result.getData().getData();
                    updateCoverUI(currentPhotoUri);
                    checkStep1Validity();
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    updateCoverUI(currentPhotoUri);
                    checkStep1Validity();
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openCameraIntent();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tu_sach, container, false);
        sessionManager = new SessionManager(requireContext());

        layoutMain = view.findViewById(R.id.layout_main_tusach);
        popupAuthorWorks = view.findViewById(R.id.popup_author_works);
        popupCreateStep1 = view.findViewById(R.id.popup_book_create_step1);
        popupCreateStep2 = view.findViewById(R.id.popup_book_create_step2);
        popupChooseCategory = view.findViewById(R.id.popup_choose_category);

        initRecentStoryViews(view);
        initCreateBookViews(view);
        setupPopupsNavigation(view);
        loadUserStories(false);
        loadAllGenres();

        return view;
    }

    private void initRecentStoryViews(View view) {
        btnEditRecent = view.findViewById(R.id.btn_edit_meo);
        if (btnEditRecent instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) btnEditRecent;
            ivRecentCover = (RoundedImageView) vg.getChildAt(0);
            LinearLayout textInfo = (LinearLayout) vg.getChildAt(1);
            tvRecentTitle = (TextView) textInfo.getChildAt(1);
            tvRecentStatus = (TextView) textInfo.getChildAt(2);
        }
        containerMyWorks = view.findViewById(R.id.container_my_works_list);
    }

    private void initCreateBookViews(View view) {
        // Bước 1
        etBookTitle = view.findViewById(R.id.et_book_title);
        etBookDesc = view.findViewById(R.id.et_book_description);
        btnAddCover = view.findViewById(R.id.btn_add_cover);
        ivCoverPreview = view.findViewById(R.id.iv_cover_preview);
        ivAddIcon = view.findViewById(R.id.iv_add_icon);
        btnNextStep1 = view.findViewById(R.id.btn_next_create1);

        TextWatcher step1Watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { checkStep1Validity(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etBookTitle.addTextChangedListener(step1Watcher);
        etBookDesc.addTextChangedListener(step1Watcher);
        btnAddCover.setOnClickListener(v -> showImagePickerDialog());

        // Bước 2
        tvStep2Title = view.findViewById(R.id.tv_book_title_display);
        tvStep2Desc = view.findViewById(R.id.tv_book_desc_display);
        tvStep2Category = view.findViewById(R.id.tv_book_categories_display);
        tvStep2StatusLabel = view.findViewById(R.id.tv_status_label_create);
        switchStatusCreate = view.findViewById(R.id.switch_status_create);
        btnDoneCreate = view.findViewById(R.id.btn_done_create2);
        
        popupEditTitleStep2 = view.findViewById(R.id.popup_edit_title_step2);
        popupEditDescStep2 = view.findViewById(R.id.popup_edit_desc_step2);
        etEditTitleStep2 = view.findViewById(R.id.et_edit_title_step2);
        etEditDescStep2 = view.findViewById(R.id.et_edit_desc_step2);
        ivBookCoverStep2 = view.findViewById(R.id.iv_book_cover_step2);

        switchStatusCreate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvStep2StatusLabel.setText(isChecked ? "Đã hoàn thành" : "Đang tiến hành");
            tvStep2StatusLabel.setTextColor(isChecked ? Color.parseColor("#4CAF50") : Color.parseColor("#888888"));
        });

        view.findViewById(R.id.layout_edit_category_pop).setOnClickListener(v -> {
            setupCategoryListView();
            popupCreateStep2.setVisibility(View.GONE);
            popupChooseCategory.setVisibility(View.VISIBLE);
        });

        View coverLayout = view.findViewById(R.id.layout_edit_cover_step2);
        if (coverLayout != null) coverLayout.setOnClickListener(v -> showImagePickerDialog());

        View titleLayout = view.findViewById(R.id.layout_edit_title_pop);
        if (titleLayout != null) titleLayout.setOnClickListener(v -> {
            etEditTitleStep2.setText(tvStep2Title.getText().toString());
            popupCreateStep2.setVisibility(View.GONE);
            popupEditTitleStep2.setVisibility(View.VISIBLE);
        });

        View descLayout = view.findViewById(R.id.layout_edit_desc_pop);
        if (descLayout != null) descLayout.setOnClickListener(v -> {
            etEditDescStep2.setText(tvStep2Desc.getText().toString());
            popupCreateStep2.setVisibility(View.GONE);
            popupEditDescStep2.setVisibility(View.VISIBLE);
        });

        View btnBackTitle = view.findViewById(R.id.btn_back_title_step2);
        if (btnBackTitle != null) btnBackTitle.setOnClickListener(v -> {
            tvStep2Title.setText(etEditTitleStep2.getText().toString());
            etBookTitle.setText(etEditTitleStep2.getText().toString());
            popupEditTitleStep2.setVisibility(View.GONE);
            popupCreateStep2.setVisibility(View.VISIBLE);
            checkStep2Validity();
        });

        View btnBackDesc = view.findViewById(R.id.btn_back_desc_step2);
        if (btnBackDesc != null) btnBackDesc.setOnClickListener(v -> {
            tvStep2Desc.setText(etEditDescStep2.getText().toString());
            etBookDesc.setText(etEditDescStep2.getText().toString());
            popupEditDescStep2.setVisibility(View.GONE);
            popupCreateStep2.setVisibility(View.VISIBLE);
            checkStep2Validity();
        });

        btnDoneCreate.setOnClickListener(v -> createStoryOnServer());

        checkStep1Validity();
        checkStep2Validity();
    }

    private void createStoryOnServer() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;

        RequestBody titlePart = RequestBody.create(etBookTitle.getText().toString(), MediaType.parse("text/plain"));
        RequestBody descPart = RequestBody.create(etBookDesc.getText().toString(), MediaType.parse("text/plain"));
        RequestBody statusPart = RequestBody.create(switchStatusCreate.isChecked() ? "hoan_thanh" : "da_dang", MediaType.parse("text/plain"));

        List<MultipartBody.Part> genreParts = new ArrayList<>();
        for (Integer id : selectedGenreIds) {
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

        Toast.makeText(getContext(), "Đang tạo truyện...", Toast.LENGTH_SHORT).show();
        RetrofitClient.getApi().createStory(token, titlePart, descPart, statusPart, genreParts, bodyCover).enqueue(new Callback<Story>() {
            @Override
            public void onResponse(@NonNull Call<Story> call, @NonNull Response<Story> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Tạo truyện thành công!", Toast.LENGTH_SHORT).show();
                    backToMain();
                    loadUserStories(false);
                    resetFields();
                } else {
                    Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<Story> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetFields() {
        etBookTitle.setText("");
        etBookDesc.setText("");
        currentPhotoUri = null;
        if (ivCoverPreview != null) ivCoverPreview.setVisibility(View.GONE);
        if (ivAddIcon != null) ivAddIcon.setVisibility(View.VISIBLE);
        selectedGenreIds.clear();
        updateCategoryUI();
    }

    private File uriToFile(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        File tempFile = new File(requireContext().getCacheDir(), "temp_new_cover.jpg");
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

    private void loadAllGenres() {
        RetrofitClient.getApi().getGenres().enqueue(new Callback<List<Story.Genre>>() {
            @Override
            public void onResponse(@NonNull Call<List<Story.Genre>> call, @NonNull Response<List<Story.Genre>> response) {
                if (response.isSuccessful()) allGenres = response.body();
            }
            @Override public void onFailure(@NonNull Call<List<Story.Genre>> call, @NonNull Throwable t) {}
        });
    }

    private void setupCategoryListView() {
        LinearLayout container = popupChooseCategory.findViewById(R.id.container_categories);
        container.removeAllViews();
        for (Story.Genre genre : allGenres) {
            View row = getLayoutInflater().inflate(R.layout.item_category_select, container, false);
            TextView tv = row.findViewById(R.id.tv_category_name);
            ImageView ivCheck = row.findViewById(R.id.iv_check);
            tv.setText(genre.getName());
            ivCheck.setVisibility(selectedGenreIds.contains(genre.getId()) ? View.VISIBLE : View.GONE);
            row.setOnClickListener(v -> {
                if (selectedGenreIds.contains(genre.getId())) {
                    selectedGenreIds.remove(genre.getId());
                    ivCheck.setVisibility(View.GONE);
                } else {
                    selectedGenreIds.add(genre.getId());
                    ivCheck.setVisibility(View.VISIBLE);
                }
                checkStep2Validity();
            });
            container.addView(row);
        }
    }

    private void updateCategoryUI() {
        if (selectedGenreIds.isEmpty()) tvStep2Category.setText("Chưa chọn");
        else {
            List<String> names = new ArrayList<>();
            for (Story.Genre g : allGenres) if (selectedGenreIds.contains(g.getId())) names.add(g.getName());
            tvStep2Category.setText(String.join(", ", names));
        }
        checkStep2Validity();
    }

    private void checkStep1Validity() {
        boolean isValid = !etBookTitle.getText().toString().trim().isEmpty() &&
                          !etBookDesc.getText().toString().trim().isEmpty() &&
                          currentPhotoUri != null;
        if (btnNextStep1 != null) {
            btnNextStep1.setEnabled(isValid);
            btnNextStep1.setAlpha(isValid ? 1.0f : 0.3f);
            btnNextStep1.setColorFilter(isValid ? Color.parseColor("#D9A441") : Color.GRAY);
        }
    }

    private void checkStep2Validity() {
        boolean isValid = !selectedGenreIds.isEmpty();
        if (btnDoneCreate != null) {
            btnDoneCreate.setEnabled(isValid);
            btnDoneCreate.setAlpha(isValid ? 1.0f : 0.3f);
            if (isValid) btnDoneCreate.setColorFilter(Color.parseColor("#D9A441"));
            else btnDoneCreate.setColorFilter(Color.GRAY);
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new AlertDialog.Builder(requireContext()).setTitle("Thêm bìa truyện").setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCameraIntent();
                } else {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            } else galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        }).show();
    }

    private void openCameraIntent() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewCover");
        currentPhotoUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
        cameraLauncher.launch(intent);
    }

    private void updateCoverUI(Uri uri) {
        if (ivCoverPreview != null) {
            ivCoverPreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(uri).into(ivCoverPreview);
        }
        if (ivAddIcon != null) ivAddIcon.setVisibility(View.GONE);
        if (ivBookCoverStep2 != null && uri != null) {
            Glide.with(this).load(uri).into(ivBookCoverStep2);
        }
    }

    private void setupPopupsNavigation(View view) {
        view.findViewById(R.id.btn_write_new).setOnClickListener(v -> {
            layoutMain.setVisibility(View.GONE);
            popupCreateStep1.setVisibility(View.VISIBLE);
        });
        view.findViewById(R.id.btn_edit_other).setOnClickListener(v -> loadUserStories(true));
        popupCreateStep1.findViewById(R.id.btn_close_create1).setOnClickListener(v -> backToMain());
        btnNextStep1.setOnClickListener(v -> {
            tvStep2Title.setText(etBookTitle.getText().toString());
            tvStep2Desc.setText(etBookDesc.getText().toString());
            popupCreateStep1.setVisibility(View.GONE);
            popupCreateStep2.setVisibility(View.VISIBLE);
        });
        popupCreateStep2.findViewById(R.id.btn_close_create2).setOnClickListener(v -> {
            popupCreateStep2.setVisibility(View.GONE);
            popupCreateStep1.setVisibility(View.VISIBLE);
        });
        popupChooseCategory.findViewById(R.id.btn_back_category).setOnClickListener(v -> {
            updateCategoryUI();
            popupChooseCategory.setVisibility(View.GONE);
            popupCreateStep2.setVisibility(View.VISIBLE);
        });
        if (popupAuthorWorks != null) popupAuthorWorks.findViewById(R.id.btn_close_works).setOnClickListener(v -> backToMain());
    }

    private void loadUserStories(boolean showListPopup) {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        RetrofitClient.getApi().getUserProfile(token).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(@NonNull Call<UserProfile> call, @NonNull Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int myId = response.body().getId();
                    RetrofitClient.getApi().getStories(null, null, null, myId).enqueue(new Callback<List<Story>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Story>> call, @NonNull Response<List<Story>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Story> myStories = response.body();
                                if (!myStories.isEmpty()) {
                                    btnEditRecent.setVisibility(View.VISIBLE);
                                    updateUI(myStories.get(0), myStories, showListPopup);
                                } else {
                                    btnEditRecent.setVisibility(View.GONE);
                                    if (showListPopup) {
                                        Toast.makeText(getContext(), "Bạn chưa có tác phẩm nào", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                        @Override public void onFailure(@NonNull Call<List<Story>> call, @NonNull Throwable t) {}
                    });
                }
            }
            @Override public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t) {}
        });
    }

    private void updateUI(Story newest, List<Story> stories, boolean showListPopup) {
        tvRecentTitle.setText(newest.getTitle());
        tvRecentStatus.setText("da_dang".equals(newest.getStatus()) ? "Đã đăng tải" : "Đang tiến hành");
        Glide.with(requireContext()).load(newest.getCoverUrl()).placeholder(R.drawable.anhtruyen).into(ivRecentCover);
        btnEditRecent.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BookEditActivity.class);
            intent.putExtra("STORY_ID", newest.getId());
            startActivity(intent);
        });
        if (showListPopup) {
            displayStoriesInPopup(stories);
            layoutMain.setVisibility(View.GONE);
            popupAuthorWorks.setVisibility(View.VISIBLE);
        }
    }

    private void displayStoriesInPopup(List<Story> stories) {
        if (containerMyWorks == null) return;
        containerMyWorks.removeAllViews();
        for (Story s : stories) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_my_work, containerMyWorks, false);
            ((TextView)itemView.findViewById(R.id.tv_title)).setText(s.getTitle());
            Glide.with(this).load(s.getCoverUrl()).placeholder(R.drawable.anhtruyen).into((ImageView)itemView.findViewById(R.id.iv_cover));

            // Set stats
            TextView tvViews = itemView.findViewById(R.id.tv_views);
            if (tvViews != null) tvViews.setText("👁 " + s.getViews());
            
            TextView tvLikes = itemView.findViewById(R.id.tv_likes);
            if (tvLikes != null) tvLikes.setText("🔖 " + s.getTotalRatings());
            
            TextView tvChapters = itemView.findViewById(R.id.tv_chapters);
            if (tvChapters != null) tvChapters.setText("≡ " + s.getChaptersCount() + " chương");

            // Ignore comments because backend story model doesn't return comments count yet

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), BookEditActivity.class);
                intent.putExtra("STORY_ID", s.getId());
                startActivity(intent);
            });
            containerMyWorks.addView(itemView);
        }
    }

    private void backToMain() {
        popupAuthorWorks.setVisibility(View.GONE);
        popupCreateStep1.setVisibility(View.GONE);
        popupCreateStep2.setVisibility(View.GONE);
        if (popupEditTitleStep2 != null) popupEditTitleStep2.setVisibility(View.GONE);
        if (popupEditDescStep2 != null) popupEditDescStep2.setVisibility(View.GONE);
        if (popupChooseCategory != null) popupChooseCategory.setVisibility(View.GONE);
        layoutMain.setVisibility(View.VISIBLE);
    }
}
