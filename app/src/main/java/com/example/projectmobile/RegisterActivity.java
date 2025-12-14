package com.example.projectmobile;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnRegister;
    TextView tvLoginLink;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvGoToLogin);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Cần nhập Email");
                return;
            }
            if (password.length() < 6) {
                etPassword.setError("Mật khẩu phải >= 6 ký tự");
                return;
            }

            // Tạo User trong Auth
            fAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                FirebaseUser user = fAuth.getCurrentUser();

                // Lưu Role vào Firestore
                DocumentReference df = fStore.collection("users").document(user.getUid());
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("email", email);
                userInfo.put("role", "user"); // Mặc định là User

                df.set(userInfo).addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), UserActivity.class));
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Lỗi lưu DB: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            }).addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}