package com.example.projectmobile;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Button btnPosts = findViewById(R.id.btnManagePosts);
        Button btnCats = findViewById(R.id.btnManageCategories);
        Button btnUsers = findViewById(R.id.btnManageUsers);
        Button btnLogout = findViewById(R.id.btnLogoutAdmin);
        // Trong hàm onCreate của AdminActivity
        Button btnUser = findViewById(R.id.btnManageUsers);
        btnUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminUserActivity.class)));

        btnPosts.setOnClickListener(v -> startActivity(new Intent(this, AdminPostActivity.class)));
        btnCats.setOnClickListener(v -> startActivity(new Intent(this, AdminCategoryActivity.class)));
        // btnUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminUserActivity.class))); // Làm sau

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });
    }
}