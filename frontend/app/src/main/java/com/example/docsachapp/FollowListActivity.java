package com.example.docsachapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.docsachapp.adapter.FollowListAdapter;
import com.example.docsachapp.api.RetrofitClient;
import com.example.docsachapp.api.SessionManager;
import com.example.docsachapp.model.UserSearchItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowListActivity extends AppCompatActivity {

    private int userId;
    private String username;
    private int currentTab; // 0 for Followers, 1 for Following
    private SessionManager sessionManager;

    private TextView tvTitle;
    private ImageView btnBack;

    private LinearLayout tabFollowers, tabFollowing;
    private TextView tvTabFollowers, tvTabFollowing;
    private View indicatorFollowers, indicatorFollowing;

    private RecyclerView rvFollowList;
    private FollowListAdapter adapter;

    private List<UserSearchItem> followersList = new ArrayList<>();
    private List<UserSearchItem> followingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_followed);

        sessionManager = new SessionManager(this);
        userId = getIntent().getIntExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        currentTab = getIntent().getIntExtra("TAB_INITIAL", 0);

        if (userId == -1) {
            finish();
            return;
        }

        initViews();
        setupTabs();
        loadData();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        if (username != null) {
            tvTitle.setText(username);
        }

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        tabFollowers = findViewById(R.id.tab_followers);
        tabFollowing = findViewById(R.id.tab_following);

        tvTabFollowers = findViewById(R.id.tv_tab_followers);
        tvTabFollowing = findViewById(R.id.tv_tab_following);

        indicatorFollowers = findViewById(R.id.indicator_followers);
        indicatorFollowing = findViewById(R.id.indicator_following);

        rvFollowList = findViewById(R.id.rv_follow_list);
        rvFollowList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FollowListAdapter(new ArrayList<>(), this);
        rvFollowList.setAdapter(adapter);
    }

    private void setupTabs() {
        tabFollowers.setOnClickListener(v -> {
            if (currentTab != 0) {
                currentTab = 0;
                updateTabUI();
            }
        });

        tabFollowing.setOnClickListener(v -> {
            if (currentTab != 1) {
                currentTab = 1;
                updateTabUI();
            }
        });
    }

    private void updateTabUI() {
        int colorBlack = ContextCompat.getColor(this, R.color.black);
        int colorGray = android.graphics.Color.parseColor("#888888");
        int colorLightGray = android.graphics.Color.parseColor("#EEEEEE");

        if (currentTab == 0) {
            tvTabFollowers.setTextColor(colorBlack);
            tvTabFollowers.setTypeface(null, android.graphics.Typeface.BOLD);
            indicatorFollowers.setBackgroundColor(colorBlack);
            indicatorFollowers.getLayoutParams().height = (int) (2 * getResources().getDisplayMetrics().density);

            tvTabFollowing.setTextColor(colorGray);
            tvTabFollowing.setTypeface(null, android.graphics.Typeface.NORMAL);
            indicatorFollowing.setBackgroundColor(colorLightGray);
            indicatorFollowing.getLayoutParams().height = (int) (1 * getResources().getDisplayMetrics().density);

            adapter.updateData(followersList);
        } else {
            tvTabFollowing.setTextColor(colorBlack);
            tvTabFollowing.setTypeface(null, android.graphics.Typeface.BOLD);
            indicatorFollowing.setBackgroundColor(colorBlack);
            indicatorFollowing.getLayoutParams().height = (int) (2 * getResources().getDisplayMetrics().density);

            tvTabFollowers.setTextColor(colorGray);
            tvTabFollowers.setTypeface(null, android.graphics.Typeface.NORMAL);
            indicatorFollowers.setBackgroundColor(colorLightGray);
            indicatorFollowers.getLayoutParams().height = (int) (1 * getResources().getDisplayMetrics().density);

            adapter.updateData(followingList);
        }
        
        indicatorFollowers.requestLayout();
        indicatorFollowing.requestLayout();
    }

    private void loadData() {
        String authHeader = sessionManager.getAuthHeader();

        // Load followers - Updated to use getUserFollowers
        RetrofitClient.getApi().getUserFollowers(userId, authHeader).enqueue(new Callback<List<UserSearchItem>>() {
            @Override
            public void onResponse(Call<List<UserSearchItem>> call, Response<List<UserSearchItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    followersList = response.body();
                    tvTabFollowers.setText("Người theo dõi: " + followersList.size());
                    if (currentTab == 0) {
                        adapter.updateData(followersList);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserSearchItem>> call, Throwable t) {
                Toast.makeText(FollowListActivity.this, "Lỗi tải followers", Toast.LENGTH_SHORT).show();
            }
        });

        // Load following - Updated to use getUserFollowing
        RetrofitClient.getApi().getUserFollowing(userId, authHeader).enqueue(new Callback<List<UserSearchItem>>() {
            @Override
            public void onResponse(Call<List<UserSearchItem>> call, Response<List<UserSearchItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    followingList = response.body();
                    tvTabFollowing.setText("Đang theo dõi: " + followingList.size());
                    if (currentTab == 1) {
                        adapter.updateData(followingList);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserSearchItem>> call, Throwable t) {
                Toast.makeText(FollowListActivity.this, "Lỗi tải following", Toast.LENGTH_SHORT).show();
            }
        });

        // Initial UI rendering
        updateTabUI();
    }
}
