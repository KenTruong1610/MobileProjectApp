package com.example.projectmobile.model;



import java.util.Date;


import java.util.Date;

public class Post {
    // 1. Khai báo biến
    private String id;
    private String title;
    private String content;
    private String authorId;
    private String status;
    private Date timestamp;

    // 2. Constructor rỗng (Bắt buộc cho Firebase)
    public Post() {}

    // 3. Constructor đầy đủ
    public Post(String id, String title, String content, String authorId, String status, Date timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.status = status;
        this.timestamp = timestamp;
    }

    // 4. Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthorId() { return authorId; }
    public String getStatus() { return status; }
    public Date getTimestamp() { return timestamp; }

    // 5. SETTERS (Phần bạn đang thiếu hoặc đặt sai chỗ)
    // Phải nằm TRƯỚC dấu đóng ngoặc nhọn cuối cùng
    public void setStatus(String status) {
        this.status = status;
    }

    public void setId(String id) {
        this.id = id;
    }

} // <--- Dấu đóng ngoặc của class Post phải ở đây (Cuối cùng)