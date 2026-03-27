package com.example.docsachapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SearchFragment extends Fragment {
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        
        EditText etSearch = view.findViewById(R.id.et_search);
        TextView tvCancel = view.findViewById(R.id.tv_cancel);
        LinearLayout llRecent = view.findViewById(R.id.ll_recent_searches);
        Spinner spinner = view.findViewById(R.id.spinner_categories);
        ImageView ivClearHistory = view.findViewById(R.id.iv_clear_history);
        
        // Setup dropdown categories
        String[] categories = {"Tất cả", "Lãng mạn", "Truyện ngắn", "Kỹ năng sống", "Khoa học viễn tưởng", "Hành động"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(adapter);

        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tvCancel.setVisibility(View.VISIBLE);
                llRecent.setVisibility(View.VISIBLE);
            } else {
                tvCancel.setVisibility(View.GONE);
                llRecent.setVisibility(View.GONE);
            }
        });

        tvCancel.setOnClickListener(v -> {
            etSearch.clearFocus();
            etSearch.setText("");
        });
        
        ivClearHistory.setOnClickListener(v -> {
            llRecent.setVisibility(View.GONE);
        });

        // Request focus immediately as specified in prompt for this fragment
        etSearch.requestFocus();

        return view;
    }
}
