package com.example.projectmobile.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

public class Post implements Serializable {
    // Giữ lại các trường cũ
    private String id;
    private String title;
    private String content;
    private String imageUrl;
    private String category;
    private String userId;
    private String userEmail;
    private String status;
    private Date timestamp;

    // Thêm trường postId. @Exclude để không lưu trường này 2 lần vào Firestore
    // vì nó sẽ được dùng làm ID của document.
    @Exclude
    private String postId;

    // Constructor rỗng (Bắt buộc cho Firebase)
    public Post() { }

    // Constructor đầy đủ (Không cần thay đổi)
    public Post(String id, String title, String content, String imageUrl, String category, String userId, String userEmail, String status, Date timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.category = category;
        this.userId = userId;
        this.userEmail = userEmail;
        this.status = status;
        this.timestamp = timestamp;
    }

    // --- GETTER & SETTER MỚI CHO POSTID ---
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    // --- CÁC GETTER & SETTER CŨ ---
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}