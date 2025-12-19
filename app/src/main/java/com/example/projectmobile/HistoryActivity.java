package com.example.projectmobile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmobile.model.Post;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";

    private RecyclerView recyclerHistory;
    private HistoryAdapter historyAdapter;
    private List<Post> postList;
    private FloatingActionButton fabDeleteHistory;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ Views
        recyclerHistory = findViewById(R.id.recyclerHistory);
        fabDeleteHistory = findViewById(R.id.fabDeleteHistory);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách và adapter
        postList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(postList, this);
        recyclerHistory.setAdapter(historyAdapter);

        // Gắn sự kiện cho nút xóa
        fabDeleteHistory.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại lịch sử mỗi khi quay lại màn hình này
        loadHistory();
    }

    private void loadHistory() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("users").document(userId).collection("history")
                .orderBy("readAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(historySnapshots -> {
                    if (historySnapshots.isEmpty()) {
                        postList.clear();
                        historyAdapter.notifyDataSetChanged();
                        fabDeleteHistory.setVisibility(View.GONE); // Ẩn nút xóa nếu không có lịch sử
                        Toast.makeText(this, "Lịch sử đọc của bạn trống.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fabDeleteHistory.setVisibility(View.VISIBLE); // Hiện nút xóa nếu có lịch sử

                    List<String> postIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : historySnapshots) {
                        String postId = doc.getString("postId");
                        if (postId != null && !postId.isEmpty()) {
                            postIds.add(postId);
                        }
                    }

                    if (!postIds.isEmpty()) {
                        fetchPostsDetails(postIds);
                    } else {
                        postList.clear();
                        historyAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải lịch sử", e);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchPostsDetails(final List<String> postIds) {
        db.collection("posts").whereIn(FieldPath.documentId(), postIds)
                .get()
                .addOnSuccessListener(postsSnapshots -> {
                    postList.clear();
                    if (!postsSnapshots.isEmpty()) {
                        for (DocumentSnapshot postDoc : postsSnapshots) {
                            Post post = postDoc.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(postDoc.getId());
                                postList.add(post);
                            }
                        }
                        Collections.sort(postList, Comparator.comparingInt(p -> postIds.indexOf(p.getPostId())));
                        historyAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải chi tiết bài viết", e);
                });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa Lịch Sử")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử đọc báo không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteHistory();
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteHistory() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        CollectionReference historyRef = db.collection("users").document(userId).collection("history");

        // Xóa tất cả các document trong sub-collection 'history' theo batch
        historyRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                batch.delete(doc.getReference());
            }
            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(HistoryActivity.this, "Đã xóa toàn bộ lịch sử.", Toast.LENGTH_SHORT).show();
                postList.clear();
                historyAdapter.notifyDataSetChanged();
                fabDeleteHistory.setVisibility(View.GONE);
            }).addOnFailureListener(e -> {
                Toast.makeText(HistoryActivity.this, "Xóa lịch sử thất bại.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi khi xóa lịch sử", e);
            });
        });
    }
}
