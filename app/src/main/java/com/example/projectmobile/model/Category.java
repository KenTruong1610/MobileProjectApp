package com.example.projectmobile.model;


public class Category {
    String id, name;

    public Category() {}

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() { return name; }
    public String getId() { return id; }
}