package com.example.projectmobile;


import com.example.projectmobile.model.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    // Đường dẫn: data/2.5/weather?q={city}&appid={key}&units=metric&lang=vi
    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units, // Để lấy độ C
            @Query("lang") String lang    // Để lấy tiếng Việt
    );
}