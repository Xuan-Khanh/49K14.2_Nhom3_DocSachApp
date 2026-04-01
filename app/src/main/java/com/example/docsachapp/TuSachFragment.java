package com.example.docsachapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TuSachFragment extends Fragment {

    private View layoutMain, popupCreateStep1, popupCreateStep2, popupAuthorWorks;
    private View popupCategory, popupEditTitle, popupEditDesc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tu_sach, container, false);

        // 1. Ánh xạ các vùng giao diện (Popup)
        layoutMain = view.findViewById(R.id.layout_main_tusach);
        popupAuthorWorks = view.findViewById(R.id.popup_author_works);
        popupCreateStep1 = view.findViewById(R.id.popup_book_create_step1);
        popupCreateStep2 = view.findViewById(R.id.popup_book_create_step2);
        popupCategory = view.findViewById(R.id.popup_choose_category);
        popupEditTitle = view.findViewById(R.id.popup_edit_title);
        popupEditDesc = view.findViewById(R.id.popup_edit_description);

        // 2. Xử lý nút bấm ở màn hình chính Tủ Sách
        view.findViewById(R.id.btn_write_new).setOnClickListener(v -> showPopup(popupCreateStep1));
        view.findViewById(R.id.btn_edit_other).setOnClickListener(v -> showPopup(popupAuthorWorks));

        // 3. Xử lý các nút điều hướng trong Popup Tạo truyện Bước 1
        popupCreateStep1.findViewById(R.id.btn_close_create1).setOnClickListener(v -> backToMain());
        popupCreateStep1.findViewById(R.id.btn_next_create1).setOnClickListener(v -> showPopup(popupCreateStep2));

        // 4. Xử lý các nút điều hướng trong Popup Tạo truyện Bước 2
        popupCreateStep2.findViewById(R.id.btn_close_create2).setOnClickListener(v -> showPopup(popupCreateStep1));
        
        // Bấm vào dòng Tiêu đề trong Bước 2 để mở popup sửa tiêu đề
        popupCreateStep2.findViewById(R.id.layout_edit_title_pop).setOnClickListener(v -> showPopup(popupEditTitle));
        
        // Bấm vào dòng Mô tả trong Bước 2 để mở popup sửa mô tả
        popupCreateStep2.findViewById(R.id.layout_edit_desc_pop).setOnClickListener(v -> showPopup(popupEditDesc));
        
        // Bấm vào dòng Thể loại trong Bước 2 để mở popup chọn thể loại
        popupCreateStep2.findViewById(R.id.layout_edit_category_pop).setOnClickListener(v -> showPopup(popupCategory));

        // 5. Nút quay lại của các popup nhỏ
        popupAuthorWorks.findViewById(R.id.btn_close_works).setOnClickListener(v -> backToMain());
        popupCategory.findViewById(R.id.btn_back_category).setOnClickListener(v -> showPopup(popupCreateStep2));
        popupEditTitle.findViewById(R.id.btn_back_title).setOnClickListener(v -> showPopup(popupCreateStep2));
        popupEditDesc.findViewById(R.id.btn_back_desc).setOnClickListener(v -> showPopup(popupCreateStep2));

        return view;
    }

    // Hàm hiển thị popup và ẩn các cái khác
    private void showPopup(View popupToShow) {
        layoutMain.setVisibility(View.GONE);
        popupAuthorWorks.setVisibility(View.GONE);
        popupCreateStep1.setVisibility(View.GONE);
        popupCreateStep2.setVisibility(View.GONE);
        popupCategory.setVisibility(View.GONE);
        popupEditTitle.setVisibility(View.GONE);
        popupEditDesc.setVisibility(View.GONE);

        popupToShow.setVisibility(View.VISIBLE);
    }

    // Hàm quay về màn hình Tủ Sách chính
    private void backToMain() {
        showPopup(layoutMain);
        layoutMain.setVisibility(View.VISIBLE);
    }
}
