package com.example.projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmobile.model.Post;
import com.example.projectmobile.model.WeatherResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // --- SỬA LỖI: Ánh xạ và Cấu hình RecyclerView ---
        recyclerPosts = findViewById(R.id.recyclerUserPosts); // Đảm bảo ID này khớp với file XML
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new UserPostAdapter(postList, this);
        recyclerPosts.setAdapter(postAdapter);
        // ------------------------------------------------

        FloatingActionButton fabCreate = findViewById(R.id.fabCreate); // ID thường là fabCreatePost
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

        // Gọi các hàm tải dữ liệu
        getWeatherData();
    }

    // Nên gọi load bài viết ở onResume để khi đăng bài xong quay lại nó tự cập nhật
    @Override
    protected void onResume() {
        super.onResume();
        loadApprovedPosts();
    }

    // --- HÀM 1: LẤY BÀI VIẾT TỪ FIREBASE (Đã đưa ra ngoài) ---
    private void loadApprovedPosts() {
        FirebaseFirestore.getInstance().collection("posts")
                .whereEqualTo("status", "approved") // Chỉ lấy bài đã duyệt
                .get()
                .addOnSuccessListener(snapshots -> {
                    postList.clear(); // Xóa list cũ tránh trùng lặp
                    if (!snapshots.isEmpty()) {
                        for (DocumentSnapshot doc : snapshots) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                postList.add(post);
                            }
                        }
                        postAdapter.notifyDataSetChanged(); // Cập nhật giao diện
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserActivity.this, "Lỗi tải bài: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- HÀM 2: GỌI API THỜI TIẾT ---
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