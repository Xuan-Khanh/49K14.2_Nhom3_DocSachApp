package com.example.docsachapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.ChipGroup;

public class SearchFragment extends Fragment {
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        
        EditText etSearch = view.findViewById(R.id.et_search);
        ChipGroup cgCategoriesCollapsed = view.findViewById(R.id.cg_categories_collapsed);
        ImageView ivToggleCategories = view.findViewById(R.id.iv_toggle_categories);

        // Setup search bar focus listener
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // Hide keyboard when focus is lost
                etSearch.setText("");
            }
        });

        // Setup category toggle
        ivToggleCategories.setOnClickListener(v -> {
            // Toggle category visibility
            if (cgCategoriesCollapsed.getVisibility() == View.VISIBLE) {
                cgCategoriesCollapsed.setVisibility(View.GONE);
            } else {
                cgCategoriesCollapsed.setVisibility(View.VISIBLE);
            }
        });

        // Request focus immediately
        etSearch.requestFocus();

        return view;
    }
}
