package com.example.projectmobile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmobile.model.Category; // IMPORT MỚI
import com.example.projectmobile.model.Post;
import com.example.projectmobile.model.WeatherResponse;
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
    RecyclerView recyclerPosts;
    UserPostAdapter postAdapter;
    List<Post> postList;

    TextView tvCity, tvTemp, tvDesc, tvMsg;
    ImageView imgIcon;
    Button btnLogout;

    // KHAI BÁO BIẾN MỚI
    ImageView iconSearch;
    LinearLayout categoryFilterContainer;

    List<Category> categoryList; // LIST CATEGORY MỚI
    private TextView selectedCategoryView; // VIEW HIỆN TẠI ĐANG ĐƯỢC CHỌN

    // Biến trạng thái hiện tại
    private String currentCategory = "Tất cả";
    private String currentSearchQuery = "";

    // Cấu hình API Thời tiết
    final String BASE_URL = "https://api.openweathermap.org/";
    final String API_KEY = "89d418e26e99bc878719355d91cf78b0";
    final String CITY = "Ho Chi Minh";

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
        btnLogout = findViewById(R.id.btnLogoutUser);

        // ÁNH XẠ CÁC VIEW MỚI
        iconSearch = findViewById(R.id.iconSearch);
        categoryFilterContainer = findViewById(R.id.category_filter_container);

        // Cấu hình RecyclerView
        recyclerPosts = findViewById(R.id.recyclerUserPosts);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        categoryList = new ArrayList<>(); // KHỞI TẠO LIST CATEGORY

        postAdapter = new UserPostAdapter(postList, this);
        recyclerPosts.setAdapter(postAdapter);

        // Cấu hình FAB
        FloatingActionButton fabCreate = findViewById(R.id.fabCreate);
        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

        // Nút đăng xuất
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        // Gọi hàm cấu hình tính năng mới
        loadCategories(); // TẢI DANH MỤC ĐỘNG VÀ TẠO UI LỌC
        setupSearch();

        // Gọi các hàm tải dữ liệu
        getWeatherData();
    }

    // Nên gọi load bài viết ở onResume để khi đăng bài xong quay lại nó tự cập nhật
    @Override
    protected void onResume() {
        super.onResume();
        // Chỉ tải bài viết nếu đã có danh mục (tránh lỗi categoryList rỗng)
        if (!categoryList.isEmpty()) {
            loadApprovedPosts(currentCategory, currentSearchQuery);
        }
    }

    // --- HÀM 0: TẢI DANH MỤC TỪ FIREBASE (MỚI) ---
    private void loadCategories() {
        FirebaseFirestore.getInstance().collection("categories")
                .get()
                .addOnSuccessListener(snapshots -> {
                    categoryList.clear();

                    // Thêm danh mục "Tất cả" cố định đầu tiên
                    Category allCat = new Category("all", "Tất cả");
                    categoryList.add(allCat);

                    if (!snapshots.isEmpty()) {
                        for (DocumentSnapshot doc : snapshots) {
                            Category category = doc.toObject(Category.class);
                            if (category != null) {
                                categoryList.add(category);
                            }
                        }
                    }

                    // Sau khi tải xong, gọi hàm tạo giao diện lọc
                    setupCategoryFilter();

                    // Tải bài viết lần đầu (sẽ hiển thị danh mục "Tất cả")
                    loadApprovedPosts(currentCategory, currentSearchQuery);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserActivity.this, "Lỗi tải danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    // Trường hợp lỗi, vẫn tạo một danh mục "Tất cả" mặc định để không lỗi
                    if (categoryList.isEmpty()) {
                        categoryList.add(new Category("all", "Tất cả"));
                        setupCategoryFilter();
                        loadApprovedPosts(currentCategory, currentSearchQuery);
                    }
                });
    }


    // --- HÀM 1: LẤY BÀI VIẾT TỪ FIREBASE (ĐÃ CẬP NHẬT ĐỂ HỖ TRỢ LỌC VÀ TÌM KIẾM) ---
    private void loadApprovedPosts(String category, String query) {
        Query firebaseQuery = FirebaseFirestore.getInstance().collection("posts")
                .whereEqualTo("status", "approved");

        // 1. Lọc theo Danh mục (nếu không phải "Tất cả")
        if (category != null && !category.equals("Tất cả")) {
            // Trường 'category' trong Post.java phải khớp với tên trong Firebase
            firebaseQuery = firebaseQuery.whereEqualTo("category", category);
        }

        firebaseQuery.get()
                .addOnSuccessListener(snapshots -> {
                    postList.clear();
                    if (!snapshots.isEmpty()) {
                        for (DocumentSnapshot doc : snapshots) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                // Xử lý lọc theo từ khóa tìm kiếm (trên client)
                                boolean matchesSearch = true;
                                if (!query.isEmpty()) {
                                    String title = post.getTitle().toLowerCase();
                                    String content = post.getContent().toLowerCase();
                                    String searchLower = query.toLowerCase();

                                    if (!title.contains(searchLower) && !content.contains(searchLower)) {
                                        matchesSearch = false;
                                    }
                                }

                                if (matchesSearch) {
                                    postList.add(post);
                                }
                            }
                        }
                        postAdapter.notifyDataSetChanged();

                        if (postList.isEmpty() && !query.isEmpty()) {
                            Toast.makeText(UserActivity.this, "Không tìm thấy bài viết nào cho \"" + query + "\"", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        postAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserActivity.this, "Lỗi tải bài: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- HÀM 3: THIẾT LẬP CHỨC NĂNG LỌC THEO DANH MỤC (CẬP NHẬT LẠI) ---
    private void setupCategoryFilter() {
        // Xóa tất cả View cũ trong container
        categoryFilterContainer.removeAllViews();
        selectedCategoryView = null; // Reset View đang được chọn

        // Duyệt qua danh sách Category đã tải
        for (int i = 0; i < categoryList.size(); i++) {
            Category cat = categoryList.get(i);

            // 1. Tạo mới TextView
            TextView categoryView = new TextView(this);
            categoryView.setText(cat.getName());

            // 2. Thiết lập Layout Params
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            int marginEnd = (int) (8 * getResources().getDisplayMetrics().density);
            params.setMarginEnd(marginEnd);
            categoryView.setLayoutParams(params);

            // 3. Thiết lập Style (Padding)
            int paddingH = (int) (12 * getResources().getDisplayMetrics().density);
            int paddingV = (int) (6 * getResources().getDisplayMetrics().density);
            categoryView.setPadding(paddingH, paddingV, paddingH, paddingV);

            // 4. Thiết lập trạng thái ban đầu
            if (cat.getName().equals(currentCategory)) { // ĐANG CHỌN
                categoryView.setBackgroundResource(R.drawable.bg_category_selected);
                categoryView.setTextColor(ContextCompat.getColor(this, R.color.white));
                selectedCategoryView = categoryView;
            } else { // CHƯA ĐƯỢC CHỌN (SỬ DỤNG DRAWABLE MỚI VÀ MÀU CHỮ TRẮNG)
                categoryView.setBackgroundResource(R.drawable.bg_category_unselected);
                categoryView.setTextColor(ContextCompat.getColor(this, R.color.text_white)); // Dùng màu trắng/text_white trên nền xám
            }

            // 5. Gắn sự kiện click
            categoryView.setOnClickListener(v -> {
                // Xử lý đổi màu View cũ (RESET VỀ TRẠNG THÁI CHƯA CHỌN: BG XÁM, TEXT TRẮNG)
                if (selectedCategoryView != null) {
                    selectedCategoryView.setBackgroundResource(R.drawable.bg_category_unselected);
                    selectedCategoryView.setTextColor(ContextCompat.getColor(this, R.color.text_white)); // Đặt lại thành màu trắng
                }

                // Xử lý đổi màu View mới (TRẠNG THÁI ĐANG CHỌN: BG XANH, TEXT TRẮNG)
                categoryView.setBackgroundResource(R.drawable.bg_category_selected);
                categoryView.setTextColor(ContextCompat.getColor(this, R.color.white));
                selectedCategoryView = categoryView;

                // Lọc dữ liệu
                currentCategory = cat.getName();
                currentSearchQuery = "";
                loadApprovedPosts(currentCategory, currentSearchQuery);
            });

            // 6. Thêm View vào container
            categoryFilterContainer.addView(categoryView);
        }
    }

    // --- HÀM 4: THIẾT LẬP CHỨC NĂNG TÌM KIẾM (SỬ DỤNG ALERT DIALOG) ---
    private void setupSearch() {
        iconSearch.setOnClickListener(v -> {
            // Khởi tạo Intent để chuyển sang SearchActivity
            Intent intent = new Intent(UserActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }
    // --- HÀM 2: GỌI API THỜI TIẾT (GIỮ NGUYÊN) ---
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