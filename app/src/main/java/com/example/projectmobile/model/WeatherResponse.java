package com.example.projectmobile.model;

import java.util.List;

public class WeatherResponse {
    public Main main;
    public List<Weather> weather;
    public String name; // Tên thành phố

    public static class Main {
        public float temp;
        public float humidity;
    }

    public static class Weather {
        public String description; // Ví dụ: "mưa nhẹ"
        public String icon;        // Mã icon ảnh (vd: 10d)
    }
}

