package com.example.projectmobile;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmobile.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminUserActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AdminUserAdapter adapter;
    List<User> userList;
    FirebaseFirestore db;

    // Thêm biến cho UI mới
    TextView tvTotalUsers;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);

        // Ánh xạ View
        recyclerView = findViewById(R.id.recyclerUsers);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        btnBack = findViewById(R.id.btnBackUser);

        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new AdminUserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadUsers();
    }

    private void loadUsers() {
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                userList.clear();
                for (DocumentSnapshot d : queryDocumentSnapshots) {
                    User user = d.toObject(User.class);
                    if (user != null) {
                        user.setUid(d.getId());
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();

                // Cập nhật số lượng lên thẻ thống kê
                tvTotalUsers.setText(String.valueOf(userList.size()));
            } else {
                tvTotalUsers.setText("0");
                Toast.makeText(this, "Chưa có user nào", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}