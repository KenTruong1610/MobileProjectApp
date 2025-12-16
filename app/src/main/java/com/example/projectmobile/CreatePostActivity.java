package com.example.projectmobile;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {

    EditText etTitle, etContent, etImageUrl;
    Spinner spinnerCategory;
    Button btnPost;

    FirebaseFirestore db;
    FirebaseAuth fAuth; // Để lấy ID người đăng

    List<String> categoryList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // 1. Ánh xạ
        etImageUrl = findViewById(R.id.etImageUrl);
        etTitle = findViewById(R.id.etPostTitle);
        etContent = findViewById(R.id.etPostContent);
        spinnerCategory = findViewById(R.id.spinnerCategories);
        btnPost = findViewById(R.id.btnSubmitPost);

        // 2. Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // 3. Setup Spinner danh mục
        categoryList = new ArrayList<>();
        categoryList.add("Đang tải danh mục...");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
        spinnerCategory.setAdapter(adapter);

        // Load danh mục
        loadCategoriesFromFirebase();

        // 4. Sự kiện Đăng bài
        btnPost.setOnClickListener(v -> handlePostSubmission());
    }

    private void loadCategoriesFromFirebase() {
        db.collection("categories").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        categoryList.clear();
                        for (DocumentSnapshot d : queryDocumentSnapshots) {
                            String catName = d.getString("name");
                            if (catName != null) categoryList.add(catName);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        categoryList.clear();
                        categoryList.add("Chưa có danh mục");
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void handlePostSubmission() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();
        String category = "";

        if (spinnerCategory.getSelectedItem() != null) {
            category = spinnerCategory.getSelectedItem().toString();
        }

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và nội dung!", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- PHẦN QUAN TRỌNG NHẤT: TẠO DỮ LIỆU ĐỂ ĐẨY LÊN FIREBASE ---

        btnPost.setEnabled(false); // Khóa nút để tránh bấm nhiều lần
        btnPost.setText("Đang gửi...");

        String postId = UUID.randomUUID().toString(); // Tạo ID ngẫu nhiên cho bài viết
        FirebaseUser currentUser = fAuth.getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "anonymous";
        String userEmail = (currentUser != null) ? currentUser.getEmail() : "Ẩn danh";

        // Tạo Map chứa dữ liệu (Khớp với các trường trong class Post của bạn)
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("id", postId);
        postMap.put("title", title);
        postMap.put("content", content);
        postMap.put("imageUrl", imageUrl); // Link ảnh URL
        postMap.put("category", category);
        postMap.put("userId", userId);
        postMap.put("userEmail", userEmail); // Lưu thêm email để Admin biết ai đăng
        postMap.put("status", "pending"); // <--- QUAN TRỌNG: Mặc định là CHỜ DUYỆT
        postMap.put("timestamp", new java.util.Date());

        // Lưu lên Firestore collection "posts"
        db.collection("posts").document(postId).set(postMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã gửi bài! Vui lòng chờ Admin duyệt.", Toast.LENGTH_LONG).show();
                    finish(); // Đóng màn hình
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPost.setEnabled(true);
                    btnPost.setText("ĐĂNG BÀI");
                });
    }
}