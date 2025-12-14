package com.example.projectmobile;



import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmobile.model.Post;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AdminPostActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AdminPostAdapter adapter;
    List<Post> postList;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_post); // Nhớ tạo layout này chứa RecyclerView

        recyclerView = findViewById(R.id.recyclerAdminPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        adapter = new AdminPostAdapter(postList, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Load tất cả bài viết
        loadPosts();
    }

    private void loadPosts() {
        db.collection("posts").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (DocumentSnapshot d : queryDocumentSnapshots) {
                    Post post = d.toObject(Post.class);
                    // Gán ID thủ công nếu toObject không tự lấy (để an toàn khi xóa/sửa)
                    if (post != null) {
                        // Lưu ý: post.id nên được lưu trong field, hoặc lấy từ d.getId()
                        // Ở bước thêm bài viết, ta sẽ lưu luôn ID vào field
                        postList.add(post);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}