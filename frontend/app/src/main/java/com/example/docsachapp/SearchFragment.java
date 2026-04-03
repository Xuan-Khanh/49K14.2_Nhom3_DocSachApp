package com.example.docsachapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.adapter.StoryVerticalAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.model.Story;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private RecyclerView rvSearchResults;
    private StoryVerticalAdapter adapter;
    private List<Story> searchList = new ArrayList<>();
    private EditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        etSearch = view.findViewById(R.id.et_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        ChipGroup cgCategoriesCollapsed = view.findViewById(R.id.cg_categories_collapsed);
        ImageView ivToggleCategories = view.findViewById(R.id.iv_toggle_categories);

        adapter = new StoryVerticalAdapter(searchList, getContext());
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            private android.os.Handler handler = new android.os.Handler();
            private Runnable searchRunnable;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    searchList.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }
                searchRunnable = () -> callSearchApi(keyword);
                handler.postDelayed(searchRunnable, 500);
            }
        });

        ivToggleCategories.setOnClickListener(v -> {
            if (cgCategoriesCollapsed.getVisibility() == View.VISIBLE) {
                cgCategoriesCollapsed.setVisibility(View.GONE);
            } else {
                cgCategoriesCollapsed.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void callSearchApi(String keyword) {
        if (!isAdded()) return;

        RetrofitClient.getApi().searchStories(keyword).enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchList.clear();
                    searchList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}