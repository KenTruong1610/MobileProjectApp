package com.example.projectmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // Khai báo các View
    private ImageView ivProfile;
    private TextView tvName, tvEmail, tvDateOfBirth, tvGender, tvPhone, tvAddress;
    private Button btnEditProfile, btnLogout;
    private ImageButton btnChangeTheme;

    // Khai báo Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo Firebase Auth và Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Khởi tạo SharedPreferences để lưu lựa chọn theme
        sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE);

        // Ánh xạ View từ layout
        ivProfile = findViewById(R.id.ivProfile);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth);
        tvGender = findViewById(R.id.tvGender);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        btnEditProfile = findViewById(R.id.button);
        btnLogout = findViewById(R.id.btnLogoutUser);
        btnChangeTheme = findViewById(R.id.btnChangeTheme);

        // Thiết lập nút đổi theme
        setupThemeButton();

        // Sự kiện cho nút chỉnh sửa
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Sự kiện cho nút đăng xuất
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupThemeButton() {
        btnChangeTheme.setOnClickListener(v -> {
            // Lấy trạng thái hiện tại
            boolean isDarkMode = sharedPreferences.getBoolean("is_dark_mode", false);

            // Đảo ngược trạng thái và lưu lại
            boolean newMode = !isDarkMode;
            sharedPreferences.edit().putBoolean("is_dark_mode", newMode).apply();

            // Áp dụng theme mới
            if (newMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String email = currentUser.getEmail();
        tvEmail.setText(email);

        String uid = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    tvName.setText(document.getString("name") != null ? document.getString("name") : "Chưa cập nhật");
                    tvDateOfBirth.setText(document.getString("dob") != null ? document.getString("dob") : "Chưa cập nhật");
                    tvGender.setText(document.getString("gender") != null ? document.getString("gender") : "Chưa cập nhật");
                    tvPhone.setText(document.getString("phone") != null ? document.getString("phone") : "Chưa cập nhật");
                    tvAddress.setText(document.getString("address") != null ? document.getString("address") : "Chưa cập nhật");

                    String avatarUrl = document.getString("avatarUrl");
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .circleCrop()
                                .into(ivProfile);
                    } else {
                        Glide.with(this).load(R.drawable.ic_default_avatar).circleCrop().into(ivProfile);
                    }
                } else {
                    tvName.setText("Chưa cập nhật");
                    Glide.with(this).load(R.drawable.ic_default_avatar).circleCrop().into(ivProfile);
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Lỗi tải hồ sơ.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi khi lấy dữ liệu Firestore: ", task.getException());
                Glide.with(this).load(R.drawable.ic_default_avatar).circleCrop().into(ivProfile);
            }
        });
    }
}
