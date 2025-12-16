package com.example.projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.DateFormat; // <-- Nhớ Import cái này
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.projectmobile.model.Post;
import com.google.firebase.firestore.FirebaseFirestore;

public class PostDetailActivity extends AppCompatActivity {

    // 1. Khai báo biến
    TextView tvTitle, tvContent, tvDate, tvCategory, tvAuthor; // Thêm tvAuthor
    ImageView imgDetail, btnBack;

    FirebaseFirestore db; // Dùng để tra cứu tên tác giả và danh mục

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // 2. Ánh xạ View (Phải khớp ID với file XML của bạn)
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvContent = findViewById(R.id.tvDetailContent);
        tvDate = findViewById(R.id.tvDetailDate);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvAuthor = findViewById(R.id.tvDetailAuthor); // Đảm bảo file XML có ID này
        imgDetail = findViewById(R.id.imgDetailPost);
        btnBack = findViewById(R.id.btnBackDetail);

        // 3. Nhận dữ liệu
        Intent intent = getIntent();
        Post post = (Post) intent.getSerializableExtra("post");

        if (post != null) {
            // --- HIỂN THỊ DỮ LIỆU CƠ BẢN ---
            tvTitle.setText(post.getTitle());
            tvContent.setText(post.getContent());

            // --- XỬ LÝ NGÀY ĐĂNG (Format đẹp) ---
            if(post.getTimestamp() != null) {
                // Chuyển đổi Date sang chuỗi "dd/MM/yyyy HH:mm"
                CharSequence dateStr = DateFormat.format("dd/MM/yyyy HH:mm", post.getTimestamp());
                tvDate.setText("Ngày đăng: " + dateStr);
            }

            // --- XỬ LÝ ẢNH ---
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                Glide.with(this).load(post.getImageUrl()).into(imgDetail);
            } else {
                imgDetail.setImageResource(R.drawable.ic_launcher_background);
            }

            // --- XỬ LÝ DANH MỤC (Lấy tên thật thay vì ID) ---
            // Nếu trong Post đã lưu tên danh mục thì hiển thị luôn
            if (post.getCategory() != null && !post.getCategory().isEmpty()) {
                tvCategory.setText("Danh mục: " + post.getCategory());
            } else {
                // Nếu chỉ có ID, phải gọi hàm lấy tên từ Firebase (xem hàm bên dưới)
                // loadCategoryName(post.getCategoryId());
                tvCategory.setText("Danh mục: Chưa xác định");
            }

            // --- XỬ LÝ TÁC GIẢ (Hiển thị Email hoặc lấy tên từ ID) ---
            if (post.getUserEmail() != null && !post.getUserEmail().isEmpty()) {
                // Cách 1: Hiển thị Email có sẵn trong bài viết (Nhanh nhất)
                tvAuthor.setText("Người đăng: " + post.getUserEmail());
            } else if (post.getUserId() != null) {
                // Cách 2: Nếu chỉ có ID, gọi Firestore để tìm tên User
                loadAuthorName(post.getUserId());
            } else {
                tvAuthor.setText("Người đăng: Ẩn danh");
            }
            if (post.getTimestamp() != null) {
                // Format ngày giờ: dd/MM/yyyy HH:mm (Ví dụ: 15/12/2025 14:30)
                CharSequence dateStr = DateFormat.format("dd/MM/yyyy HH:mm", post.getTimestamp());
                tvDate.setText("Ngày đăng: " + dateStr);
            } else {
                tvDate.setText("Ngày đăng: Vừa xong");
            }

        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy bài viết!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 4. Sự kiện quay lại
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    // Hàm phụ: Lấy tên tác giả từ bảng "users" dựa vào ID
    private void loadAuthorName(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Giả sử trong bảng users có trường "fullName" hoặc "email"
                        String name = documentSnapshot.getString("email");
                        tvAuthor.setText("Người đăng: " + name);
                    }
                });
    }
}