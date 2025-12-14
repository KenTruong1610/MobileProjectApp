package com.example.projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegisterLink;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ View
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvGoToRegister);

        // Khởi tạo Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Chuyển sang màn hình Đăng ký
        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        });

        // Xử lý sự kiện nút Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email không được để trống");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Mật khẩu không được để trống");
                return;
            }

            // Đăng nhập Firebase Auth
            fAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                checkUserRole(authResult.getUser().getUid()); // Kiểm tra quyền
            }).addOnFailureListener(e -> {
                Toast.makeText(LoginActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    // Hàm kiểm tra role trong Firestore
    private void checkUserRole(String uid) {
        DocumentReference df = fStore.collection("users").document(uid);

        df.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");

                if ("admin".equals(role)) {
                    startActivity(new Intent(getApplicationContext(), AdminActivity.class));
                    finish();
                } else if ("user".equals(role)) {
                    startActivity(new Intent(getApplicationContext(), UserActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Tài khoản chưa được cấp quyền!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tự động đăng nhập nếu đã lưu phiên trước đó
    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            checkUserRole(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
    }
}