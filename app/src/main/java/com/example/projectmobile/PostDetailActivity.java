package com.example.projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmobile.model.Comment;
import com.example.projectmobile.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity implements CommentAdapter.OnCommentDeleteListener {

    TextView tvTitle, tvContent, tvDate, tvCategory, tvAuthor;
    ImageView imgDetail, btnBack;
    EditText etComment;
    Button btnSendComment;
    RecyclerView rvComments, rvRelatedPosts;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    CommentAdapter commentAdapter;
    List<Comment> commentList;
    PostAdapter relatedPostsAdapter;
    List<Post> relatedPostsList;
    Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvContent = findViewById(R.id.tvDetailContent);
        tvDate = findViewById(R.id.tvDetailDate);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvAuthor = findViewById(R.id.tvDetailAuthor);
        imgDetail = findViewById(R.id.imgDetailPost);
        btnBack = findViewById(R.id.btnBackDetail);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        rvComments = findViewById(R.id.rvComments);
        rvRelatedPosts = findViewById(R.id.rvRelatedPosts);

        Intent intent = getIntent();
        post = (Post) intent.getSerializableExtra("post");

        if (post != null && post.getId() != null) {
            displayPostDetails();
            setupCommentSection();
            loadComments();
            setupRelatedPosts();
            loadRelatedPosts();
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy bài viết!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void displayPostDetails() {
        tvTitle.setText(post.getTitle());
        tvContent.setText(post.getContent());

        if (post.getTimestamp() != null) {
            CharSequence dateStr = DateFormat.format("dd/MM/yyyy HH:mm", post.getTimestamp());
            tvDate.setText("Ngày đăng: " + dateStr);
        }

        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(this).load(post.getImageUrl()).into(imgDetail);
        } else {
            imgDetail.setImageResource(R.drawable.ic_launcher_background);
        }

        if (post.getCategory() != null && !post.getCategory().isEmpty()) {
            tvCategory.setText("Danh mục: " + post.getCategory());
        }

        if (post.getUserEmail() != null && !post.getUserEmail().isEmpty()) {
            tvAuthor.setText("Người đăng: " + post.getUserEmail());
        } else if (post.getUserId() != null) {
            loadAuthorName(post.getUserId());
        }
    }

    private void setupCommentSection() {
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, this);
        rvComments.setAdapter(commentAdapter);

        btnSendComment.setOnClickListener(v -> sendComment());
    }

    private void sendComment() {
        String commentContent = etComment.getText().toString().trim();
        if (commentContent.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentId = db.collection("comments").document().getId();
        Comment comment = new Comment(
                commentId,
                post.getId(),
                currentUser.getUid(),
                currentUser.getEmail(),
                commentContent,
                new Date()
        );

        commentList.add(comment);
        commentAdapter.notifyItemInserted(commentList.size() - 1);
        rvComments.scrollToPosition(commentList.size() - 1);
        etComment.setText("");

        db.collection("comments").document(commentId).set(comment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PostDetailActivity.this, "Đã thêm bình luận", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PostDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    commentList.remove(comment); 
                    commentAdapter.notifyDataSetChanged();
                });
    }

    private void loadComments() {
        db.collection("comments")
                .whereEqualTo("postId", post.getId())
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        Comment comment = dc.getDocument().toObject(Comment.class);
                        comment.setId(dc.getDocument().getId());
                        switch (dc.getType()) {
                            case ADDED:
                                if (!commentList.contains(comment)) {
                                    commentList.add(comment);
                                }
                                break;
                            case MODIFIED:
                                for (int i = 0; i < commentList.size(); i++) {
                                    if (commentList.get(i).getId().equals(comment.getId())) {
                                        commentList.set(i, comment);
                                        break;
                                    }
                                }
                                break;
                            case REMOVED:
                                commentList.removeIf(c -> c.getId().equals(comment.getId()));
                                break;
                        }
                    }
                    Collections.sort(commentList, Comparator.comparing(Comment::getTimestamp));
                    commentAdapter.notifyDataSetChanged();
                });
    }

    private void loadAuthorName(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("email");
                        tvAuthor.setText("Người đăng: " + name);
                    }
                });
    }

    private void setupRelatedPosts() {
        rvRelatedPosts.setLayoutManager(new LinearLayoutManager(this));
        relatedPostsList = new ArrayList<>();
        relatedPostsAdapter = new PostAdapter(this, relatedPostsList);
        rvRelatedPosts.setAdapter(relatedPostsAdapter);
    }

    private void loadRelatedPosts() {
        if (post.getCategory() == null || post.getCategory().isEmpty()) {
            return;
        }
        db.collection("posts")
                .whereEqualTo("category", post.getCategory())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        relatedPostsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Post p = document.toObject(Post.class);
                            p.setId(document.getId());
                            // Exclude the current post from the related list
                            if (!p.getId().equals(this.post.getId())) {
                                relatedPostsList.add(p);
                            }
                        }
                        Collections.shuffle(relatedPostsList);
                        if(relatedPostsList.size() <= 5) {
                            relatedPostsAdapter.setPosts(new ArrayList<>(relatedPostsList));
                        } else {
                            relatedPostsAdapter.setPosts(new ArrayList<>(relatedPostsList.subList(0, 5)));
                        }
                    } else {
                        Toast.makeText(this, "Lỗi khi tải bài viết liên quan", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCommentDelete(String commentId) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa bình luận")
            .setMessage("Bạn có chắc chắn muốn xóa bình luận này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                db.collection("comments").document(commentId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(PostDetailActivity.this, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                        commentAdapter.removeComment(commentId);
                    })
                    .addOnFailureListener(e -> Toast.makeText(PostDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}
