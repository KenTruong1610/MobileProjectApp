package com.example.projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.projectmobile.model.WeatherResponse;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserActivity extends AppCompatActivity {

    TextView tvCity, tvTemp, tvDesc, tvMsg;
    ImageView imgIcon;
    Button btnLogout;

    // Cấu hình API
    final String BASE_URL = "https://api.openweathermap.org/";
    final String API_KEY = "89d418e26e99bc878719355d91cf78b0"; // <--- QUAN TRỌNG
    final String CITY = "Ho Chi Minh"; // Bạn có thể đổi thành "Hanoi" hoặc lấy từ GPS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Ánh xạ
        tvCity = findViewById(R.id.tvCityName);
        tvTemp = findViewById(R.id.tvTemp);
        tvDesc = findViewById(R.id.tvWeatherDesc);
        imgIcon = findViewById(R.id.imgWeatherIcon);
        tvMsg = findViewById(R.id.tvUserMsg);
        btnLogout = findViewById(R.id.btnLogoutUser);

        // Nút đăng xuất
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        // GỌI API THỜI TIẾT
        getWeatherData();
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
                    // TRƯỜNG HỢP THÀNH CÔNG
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
                    // TRƯỜNG HỢP LỖI TỪ SERVER (Vd: Sai Key, Sai tên thành phố)
                    // Code: 401 là sai Key, 404 là sai tên thành phố
                    tvCity.setText("Lỗi API: " + response.code());
                    tvDesc.setText("Kiểm tra lại Key");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                // TRƯỜNG HỢP LỖI MẠNG (Mất mạng, không có internet)
                tvCity.setText("Lỗi mạng");
                tvDesc.setText(t.getMessage());
            }
        });
    }
}