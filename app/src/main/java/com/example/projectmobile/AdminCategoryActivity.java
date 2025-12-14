package com.example.projectmobile;


import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmobile.model.Category;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminCategoryActivity extends AppCompatActivity {

    EditText etName;
    Button btnAdd;
    FirebaseFirestore db;

    // Khai báo thêm cho RecyclerView
    RecyclerView recyclerView;
    AdminCategoryAdapter adapter;
    List<Category> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category);

        etName = findViewById(R.id.etCatName);
        btnAdd = findViewById(R.id.btnAddCat);
        recyclerView = findViewById(R.id.recyclerCategories);

        db = FirebaseFirestore.getInstance();

        // Cấu hình RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryList = new ArrayList<>();
        adapter = new AdminCategoryAdapter(categoryList, this);
        recyclerView.setAdapter(adapter);

        // Load danh sách ngay khi mở màn hình
        loadCategories();

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                etName.setError("Nhập tên danh mục");
                return;
            }

            String id = UUID.randomUUID().toString();
            Map<String, Object> cat = new HashMap<>();
            cat.put("id", id);
            cat.put("name", name);

            db.collection("categories").document(id).set(cat)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
                        etName.setText("");

                        // Reload lại danh sách sau khi thêm
                        loadCategories();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    // Hàm load dữ liệu từ Firestore
    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categoryList.clear(); // Xóa list cũ để tránh trùng lặp
            if (!queryDocumentSnapshots.isEmpty()) {
                for (DocumentSnapshot d : queryDocumentSnapshots) {
                    Category cat = d.toObject(Category.class);
                    if (cat != null) {
                        categoryList.add(cat);
                    }
                }
                adapter.notifyDataSetChanged(); // Cập nhật giao diện
            }
        });
    }
}