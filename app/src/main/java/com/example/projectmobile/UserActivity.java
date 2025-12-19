package com.example.projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmobile.model.Category;
import com.example.projectmobile.model.Post;
import com.example.projectmobile.model.WeatherResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserActivity extends AppCompatActivity {

    // Khai báo biến
    private RecyclerView recyclerPosts;
    private UserPostAdapter postAdapter;
    private List<Post> postList;

    private TextView tvCity, tvTemp, tvDesc, tvMsg;
    private ImageView imgIcon;

    private LinearLayout categoryFilterContainer;
    private List<Category> categoryList;
    private TextView selectedCategoryView;

    private String currentCategory = "Tất cả";
    private String currentSearchQuery = "";

    private final String BASE_URL = "https://api.openweathermap.org/";
    private final String API_KEY = "89d418e26e99bc878719355d91cf78b0";
    private final String CITY = "Ho Chi Minh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // 1. Ánh xạ View
        tvCity = findViewById(R.id.tvCityName);
        tvTemp = findViewById(R.id.tvTemp);
        tvDesc = findViewById(R.id.tvWeatherDesc);
        imgIcon = findViewById(R.id.imgWeatherIcon);
        tvMsg = findViewById(R.id.tvUserMsg);
        categoryFilterContainer = findViewById(R.id.category_filter_container);

        // Cấu hình RecyclerView
        recyclerPosts = findViewById(R.id.recyclerUserPosts);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postAdapter = new UserPostAdapter(postList, this);
        recyclerPosts.setAdapter(postAdapter);

        // Cấu hình FAB
        FloatingActionButton fabCreate = findViewById(R.id.fabCreate);
        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

        // Thiết lập Bottom Navigation
        setupBottomNavigation();

        // Tải danh mục và bài viết
        categoryList = new ArrayList<>();
        loadCategories();

        // Tải dữ liệu thời tiết
        getWeatherData();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(UserActivity.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(UserActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(UserActivity.this, SearchActivity.class));
                return true;
            } else if (itemId == R.id.nav_home) {
                return true; // Đã ở trang chủ
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!categoryList.isEmpty()) {
            loadApprovedPosts(currentCategory, currentSearchQuery);
        }
        // Đảm bảo mục Home được chọn khi quay lại
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void loadCategories() {
        FirebaseFirestore.getInstance().collection("categories")
                .get()
                .addOnSuccessListener(snapshots -> {
                    categoryList.clear();
                    categoryList.add(new Category("all", "Tất cả"));
                    if (!snapshots.isEmpty()) {
                        for (DocumentSnapshot doc : snapshots) {
                            Category category = doc.toObject(Category.class);
                            if (category != null) {
                                categoryList.add(category);
                            }
                        }
                    }
                    setupCategoryFilter();
                    loadApprovedPosts(currentCategory, currentSearchQuery);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserActivity.this, "Lỗi tải danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (categoryList.isEmpty()) {
                        categoryList.add(new Category("all", "Tất cả"));
                        setupCategoryFilter();
                        loadApprovedPosts(currentCategory, currentSearchQuery);
                    }
                });
    }

    private void loadApprovedPosts(String category, String query) {
        Query firebaseQuery = FirebaseFirestore.getInstance().collection("posts").whereEqualTo("status", "approved");

        if (category != null && !category.equals("Tất cả")) {
            firebaseQuery = firebaseQuery.whereEqualTo("category", category);
        }

        firebaseQuery.get()
                .addOnSuccessListener(snapshots -> {
                    postList.clear();
                    if (!snapshots.isEmpty()) {
                        for (DocumentSnapshot doc : snapshots) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                boolean matchesSearch = query.isEmpty() || post.getTitle().toLowerCase().contains(query.toLowerCase()) || post.getContent().toLowerCase().contains(query.toLowerCase());
                                if (matchesSearch) {
                                    postList.add(post);
                                }
                            }
                        }
                        if (postList.isEmpty() && !query.isEmpty()) {
                            Toast.makeText(UserActivity.this, "Không tìm thấy bài viết nào cho '" + query + "'", Toast.LENGTH_SHORT).show();
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserActivity.this, "Lỗi tải bài: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupCategoryFilter() {
        categoryFilterContainer.removeAllViews();
        selectedCategoryView = null;

        for (Category cat : categoryList) {
            TextView categoryView = new TextView(this);
            categoryView.setText(cat.getName());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
            categoryView.setLayoutParams(params);

            int paddingH = (int) (12 * getResources().getDisplayMetrics().density);
            int paddingV = (int) (6 * getResources().getDisplayMetrics().density);
            categoryView.setPadding(paddingH, paddingV, paddingH, paddingV);

            if (cat.getName().equals(currentCategory)) {
                categoryView.setBackgroundResource(R.drawable.bg_category_selected);
                categoryView.setTextColor(ContextCompat.getColor(this, R.color.white));
                selectedCategoryView = categoryView;
            } else {
                categoryView.setBackgroundResource(R.drawable.bg_category_unselected);
                categoryView.setTextColor(ContextCompat.getColor(this, R.color.text_white));
            }

            categoryView.setOnClickListener(v -> {
                if (selectedCategoryView != null) {
                    selectedCategoryView.setBackgroundResource(R.drawable.bg_category_unselected);
                    selectedCategoryView.setTextColor(ContextCompat.getColor(this, R.color.text_white));
                }
                categoryView.setBackgroundResource(R.drawable.bg_category_selected);
                categoryView.setTextColor(ContextCompat.getColor(this, R.color.white));
                selectedCategoryView = categoryView;
                currentCategory = cat.getName();
                currentSearchQuery = ""; // Reset search query when category changes
                loadApprovedPosts(currentCategory, currentSearchQuery);
            });

            categoryFilterContainer.addView(categoryView);
        }
    }

    private void getWeatherData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeather(CITY, API_KEY, "metric", "vi");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse data = response.body();
                    tvCity.setText(data.name);
                    tvTemp.setText(Math.round(data.main.temp) + "°C");

                    if (!data.weather.isEmpty()) {
                        String description = data.weather.get(0).description;
                        tvDesc.setText(description.substring(0, 1).toUpperCase() + description.substring(1));
                        String iconCode = data.weather.get(0).icon;
                        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
                        Glide.with(UserActivity.this).load(iconUrl).into(imgIcon);
                    }
                } else {
                    tvCity.setText("Lỗi API");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tvCity.setText("Lỗi mạng");
            }
        });
    }
}
