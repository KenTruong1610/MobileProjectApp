package com.example.projectmobile.model;

import java.util.Date;

public class Comment {
    private String id;
    private String postId;
    private String userId;
    private String userEmail;
    private String content;
    private Date timestamp;

    public Comment() {}

    public Comment(String id, String postId, String userId, String userEmail, String content, Date timestamp) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
