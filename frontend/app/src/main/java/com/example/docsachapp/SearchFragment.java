package com.example.docsachapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.adapter.RecentSearchAdapter;
import com.example.docsachapp.adapter.StorySearchAdapter;
import com.example.docsachapp.adapter.UserSearchAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.model.SearchResultResponse;
import com.example.docsachapp.model.Story;
import com.example.docsachapp.model.UserSearchItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Fragment Tìm Kiếm: Xử lý tìm kiếm truyện/người dùng, lọc theo thể loại và xem lịch sử tìm kiếm
public class SearchFragment extends Fragment {

    private EditText etSearch;
    private TextView tvCancelSearch, tvCategoryStoryCount;
    private LinearLayout llExploreView, llRecentSearchesView, llSearchResultsView;
    private ScrollView svExpandedCategories;

    private ChipGroup cgCollapsed, cgExpanded;
    private ImageView ivToggleDown, ivToggleUp;

    private RecyclerView rvExploreResults, rvRecentSearches, rvSearchResults;
    private TabLayout tabSearchResults;

    private StorySearchAdapter exploreAdapter;
    private StorySearchAdapter storiesResultAdapter;
    private UserSearchAdapter usersResultAdapter;
    private RecentSearchAdapter recentAdapter;

    private List<Story> exploreList = new ArrayList<>();
    private List<Story> searchStoriesList = new ArrayList<>();
    private List<UserSearchItem> searchUsersList = new ArrayList<>();
    private List<String> recentSearches = new ArrayList<>();

    /** Lưu trữ danh sách ID Thể loại mà người dùng đang chọn để lọc nhiều thể loại cùng lúc */
    private Set<Integer> selectedGenreIds = new HashSet<>();

    private SharedPreferences sharedPrefs;
    private static final String PREF_NAME = "SearchPrefs";
    private static final String KEY_RECENT = "recent_searches";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        initViews(view);
        setupAdapters();
        loadRecentSearches();
        setupListeners();

        // Initial data loads
        loadExploreStories(); // Load all stories by default
        loadGenres();

        return view;
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        tvCancelSearch = view.findViewById(R.id.tv_cancel_search);
        tvCategoryStoryCount = view.findViewById(R.id.tv_category_story_count);

        llExploreView = view.findViewById(R.id.ll_explore_view);
        svExpandedCategories = view.findViewById(R.id.sv_expanded_categories);
        llRecentSearchesView = view.findViewById(R.id.ll_recent_searches_view);
        llSearchResultsView = view.findViewById(R.id.ll_search_results_view);

        cgCollapsed = view.findViewById(R.id.cg_categories_collapsed);
        cgExpanded = view.findViewById(R.id.cg_categories_expanded);
        ivToggleDown = view.findViewById(R.id.iv_toggle_categories_down);
        ivToggleUp = view.findViewById(R.id.iv_toggle_categories_up);

        rvExploreResults = view.findViewById(R.id.rv_explore_results);
        rvRecentSearches = view.findViewById(R.id.rv_recent_searches);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        tabSearchResults = view.findViewById(R.id.tab_search_results);

        sharedPrefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private void setupAdapters() {
        exploreAdapter = new StorySearchAdapter(exploreList, getContext());
        rvExploreResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExploreResults.setAdapter(exploreAdapter);

        storiesResultAdapter = new StorySearchAdapter(searchStoriesList, getContext());
        usersResultAdapter = new UserSearchAdapter(searchUsersList, getContext());

        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(storiesResultAdapter); // Default tab

        recentAdapter = new RecentSearchAdapter(recentSearches, getContext(), new RecentSearchAdapter.OnItemClickListener() {
            @Override
            public void onClick(String term) {
                etSearch.setText(term);
                etSearch.setSelection(term.length());
                performSearch(term);
            }

            @Override
            public void onDelete(String term) {
                recentSearches.remove(term);
                saveRecentSearches();
                recentAdapter.updateData(recentSearches);
            }
        });
        rvRecentSearches.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentSearches.setAdapter(recentAdapter);
    }

    private void setupListeners() {
        // Search focus
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tvCancelSearch.setVisibility(View.VISIBLE);
                if (etSearch.getText().toString().trim().isEmpty()) {
                    showState(llRecentSearchesView);
                }
            }
        });

        // Search text typed
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty() && etSearch.hasFocus()) {
                    showState(llRecentSearchesView);
                }
            }
        });

        // Perform search action from keyboard
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String keyword = etSearch.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    performSearch(keyword);
                }
                return true;
            }
            return false;
        });

        // Cancel button
        tvCancelSearch.setOnClickListener(v -> {
            etSearch.clearFocus();
            etSearch.setText("");
            hideKeyboard();
            tvCancelSearch.setVisibility(View.GONE);
            showState(llExploreView);
        });

        // Toggle Categories
        ivToggleDown.setOnClickListener(v -> showState(svExpandedCategories));
        ivToggleUp.setOnClickListener(v -> showState(llExploreView));

        // Tabs
        tabSearchResults.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    rvSearchResults.setAdapter(storiesResultAdapter);
                } else {
                    rvSearchResults.setAdapter(usersResultAdapter);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showState(View visibleView) {
        llExploreView.setVisibility(View.GONE);
        svExpandedCategories.setVisibility(View.GONE);
        llRecentSearchesView.setVisibility(View.GONE);
        llSearchResultsView.setVisibility(View.GONE);

        visibleView.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getView() != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    // ============================ API & DATA ============================

    private void loadRecentSearches() {
        String data = sharedPrefs.getString(KEY_RECENT, "");
        recentSearches.clear();
        if (!data.isEmpty()) {
            recentSearches.addAll(Arrays.asList(data.split(",")));
        }
        if (recentAdapter != null) {
            recentAdapter.updateData(recentSearches);
        }
    }

    private void saveRecentSearches() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recentSearches.size(); i++) {
            sb.append(recentSearches.get(i));
            if (i < recentSearches.size() - 1) sb.append(",");
        }
        sharedPrefs.edit().putString(KEY_RECENT, sb.toString()).apply();
    }

    private void addRecentSearch(String term) {
        if (recentSearches.contains(term)) {
            recentSearches.remove(term);
        }
        recentSearches.add(0, term); // add to top
        if (recentSearches.size() > 10) {
            recentSearches.remove(10);
        }
        saveRecentSearches();
        if (recentAdapter != null) {
            recentAdapter.updateData(recentSearches);
        }
    }

    private void loadGenres() {
        RetrofitClient.getApi().getGenres().enqueue(new Callback<List<Story.Genre>>() {
            @Override
            public void onResponse(Call<List<Story.Genre>> call, Response<List<Story.Genre>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateCategories(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<Story.Genre>> call, Throwable t) {}
        });
    }

    private void populateCategories(List<Story.Genre> genres) {
        cgCollapsed.removeAllViews();
        cgExpanded.removeAllViews();

        if (genres.isEmpty()) {
            return;
        }

        for (int i = 0; i < genres.size(); i++) {
            Story.Genre genre = genres.get(i);

            Chip chipCol = createChip(genre);
            cgCollapsed.addView(chipCol);

            Chip chipExp = createChip(genre);
            cgExpanded.addView(chipExp);
        }
    }

    /** Tạo một UI Chip cho từng thể loại, xử lý logic toggle (Bật/Tắt) */
    private Chip createChip(Story.Genre genre) {
        Chip chip = new Chip(getContext());
        chip.setText(genre.getName());
        chip.setTag(genre.getId());
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setOnClickListener(v -> {
            if (chip.isChecked()) {
                selectedGenreIds.add(genre.getId());
            } else {
                selectedGenreIds.remove(genre.getId());
            }
            updateChipsState();
            showState(llExploreView);
            loadExploreStories();
        });
        return chip;
    }

    /** Đồng bộ trạng thái checked của các Chip (đang thu gọn và mở rộng) với selectedGenreIds */
    private void updateChipsState() {
        for (int i = 0; i < cgCollapsed.getChildCount(); i++) {
            Chip c = (Chip) cgCollapsed.getChildAt(i);
            Integer id = (Integer) c.getTag();
            c.setChecked(selectedGenreIds.contains(id));
        }
        for (int i = 0; i < cgExpanded.getChildCount(); i++) {
            Chip c = (Chip) cgExpanded.getChildAt(i);
            Integer id = (Integer) c.getTag();
            c.setChecked(selectedGenreIds.contains(id));
        }
    }

    /**
     * Chuyển Set<Integer> thành chuỗi comma-separated "1,2,3"
     * để gửi đúng param theloai cho API backend: GET /api/stories/?theloai=1,2,3
     */
    private String buildGenreIdsParam() {
        if (selectedGenreIds.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (Integer id : selectedGenreIds) {
            if (sb.length() > 0) sb.append(",");
            sb.append(id);
        }
        return sb.toString();
    }

    /** ✅ FIX: Gửi theloai dạng String comma-separated thay vì Integer */
    private void loadExploreStories() {
        String genreParam = buildGenreIdsParam();
        RetrofitClient.getApi().getStories(null, genreParam, null, null).enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    exploreList.clear();
                    exploreList.addAll(response.body());
                    if (exploreAdapter != null) {
                        exploreAdapter.notifyDataSetChanged();
                    }
                    tvCategoryStoryCount.setText(exploreList.size() + " Stories");
                }
            }
            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {}
        });
    }

    /** Thực hiện gọi API tìm kiếm tổng hợp. Gửi kèm keyword và theloai đã chọn */
    private void performSearch(String keyword) {
        hideKeyboard();
        addRecentSearch(keyword);
        showState(llSearchResultsView);

        String genreParam = buildGenreIdsParam();
        Call<SearchResultResponse> call;
        if (genreParam != null) {
            call = RetrofitClient.getApi().searchAll(keyword, genreParam);
        } else {
            call = RetrofitClient.getApi().searchAll(keyword);
        }

        call.enqueue(new Callback<SearchResultResponse>() {
            @Override
            public void onResponse(Call<SearchResultResponse> call, Response<SearchResultResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchStoriesList.clear();
                    searchUsersList.clear();

                    if (response.body().getStories() != null) {
                        searchStoriesList.addAll(response.body().getStories());
                    }
                    if (response.body().getUsers() != null) {
                        searchUsersList.addAll(response.body().getUsers());
                    }

                    if (storiesResultAdapter != null) storiesResultAdapter.notifyDataSetChanged();
                    if (usersResultAdapter != null) usersResultAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<SearchResultResponse> call, Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
