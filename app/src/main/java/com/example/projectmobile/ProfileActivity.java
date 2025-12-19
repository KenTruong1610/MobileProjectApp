package com.example.projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    // Khai báo Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo Firebase Auth và Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

        // Sự kiện cho nút chỉnh sửa
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Sự kiện cho nút đăng xuất
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            // Cờ để xóa hết các Activity cũ và tạo một Task mới
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải dữ liệu người dùng mỗi khi Activity này được hiển thị để luôn cập nhật
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Nếu không có người dùng đăng nhập, chuyển về trang Login
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Email có thể lấy trực tiếp từ FirebaseUser và nó phải luôn có
        String email = currentUser.getEmail();
        tvEmail.setText(email);

        // Lấy thông tin chi tiết khác từ Firestore
        String uid = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Cập nhật giao diện với dữ liệu từ Firestore
                    tvName.setText(document.getString("name") != null ? document.getString("name") : "Chưa cập nhật");
                    tvDateOfBirth.setText(document.getString("dob") != null ? document.getString("dob") : "Chưa cập nhật");
                    tvGender.setText(document.getString("gender") != null ? document.getString("gender") : "Chưa cập nhật");
                    tvPhone.setText(document.getString("phone") != null ? document.getString("phone") : "Chưa cập nhật");
                    tvAddress.setText(document.getString("address") != null ? document.getString("address") : "Chưa cập nhật");

                    // Tải ảnh đại diện bằng Glide
                    String avatarUrl = document.getString("avatarUrl");
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.ic_default_avatar) // Ảnh mặc định trong lúc tải
                                .error(R.drawable.ic_default_avatar) // Ảnh mặc định nếu lỗi
                                .circleCrop()
                                .into(ivProfile);
                    } else {
                        Glide.with(this).load(R.drawable.ic_default_avatar).circleCrop().into(ivProfile);
                    }
                } else {
                    // Trường hợp người dùng mới, chưa có document trong Firestore
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
