package com.example.projectmobile.model;


public class User {
    private String email;
    private String role;
    private String uid;

    public User() {}

    public User(String email, String role, String uid) {
        this.email = email;
        this.role = role;
        this.uid = uid;
    }

    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getUid() { return uid; }

    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setUid(String uid) { this.uid = uid; }
}