package com.example.projectmobile;


import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);

        recyclerView = findViewById(R.id.recyclerUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        adapter = new AdminUserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadUsers();
    }

    private void loadUsers() {
        // Lấy toàn bộ document trong collection "users"
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                userList.clear();
                for (DocumentSnapshot d : queryDocumentSnapshots) {
                    // Convert dữ liệu từ Firestore sang Object User
                    User user = d.toObject(User.class);
                    // Lưu ID document vào object để dùng nếu cần
                    if (user != null) {
                        user.setUid(d.getId());
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Chưa có user nào", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}