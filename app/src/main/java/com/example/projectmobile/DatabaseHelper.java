package com.example.projectmobile; // Đổi package name theo dự án của bạn

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Tên file DB lưu trong máy (Không liên quan đến Firebase)
    private static final String DATABASE_NAME = "Sportisa.db";
    private static final int DATABASE_VERSION = 2; // Tăng version lên để cập nhật bảng

    // Tên bảng và cột
    private static final String TABLE_POSTS = "posts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_IMAGE_URL = "image_url"; // Cột mới để lưu link ảnh

    // Constructor chuẩn - Sửa lỗi "no-arg constructor"
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng có thêm cột image_url
        String createTable = "CREATE TABLE " + TABLE_POSTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_IMAGE_URL + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }

    // Hàm thêm bài viết (Nhận cả link ảnh)
    public boolean addPost(String title, String content, String category, String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_IMAGE_URL, imageUrl); // Lưu link ảnh (nếu ko có ảnh thì là chuỗi rỗng)

        long result = db.insert(TABLE_POSTS, null, values);
        return result != -1;
    }
}