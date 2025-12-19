package com.example.projectmobile;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    // Khai báo View
    private EditText etAvatarUrl, etName, etDateOfBirth, etPhone, etAddress;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    private Button btnSave;

    // Khai báo Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ Views
        etAvatarUrl = findViewById(R.id.etAvatarUrl);
        etName = findViewById(R.id.etName);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        rbOther = findViewById(R.id.rbOther);
        btnSave = findViewById(R.id.btnSave);

        // Lấy thông tin người dùng hiện tại
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            loadUserData();
        } else {
            // Nếu không có người dùng, không thể chỉnh sửa -> quay về
            Toast.makeText(this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Gắn sự kiện cho nút Lưu
        btnSave.setOnClickListener(v -> saveUserData());
    }

    // Hàm tải dữ liệu hiện có của người dùng vào các ô EditText
    private void loadUserData() {
        DocumentReference userRef = db.collection("users").document(currentUserId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                etName.setText(documentSnapshot.getString("name"));
                etDateOfBirth.setText(documentSnapshot.getString("dob"));
                etPhone.setText(documentSnapshot.getString("phone"));
                etAddress.setText(documentSnapshot.getString("address"));
                etAvatarUrl.setText(documentSnapshot.getString("avatarUrl"));

                String gender = documentSnapshot.getString("gender");
                if (gender != null) {
                    if (gender.equals("Nam")) {
                        rbMale.setChecked(true);
                    } else if (gender.equals("Nữ")) {
                        rbFemale.setChecked(true);
                    } else {
                        rbOther.setChecked(true);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi tải dữ liệu người dùng", e);
        });
    }

    // Hàm lưu dữ liệu mới vào Firestore
    private void saveUserData() {
        String avatarUrl = etAvatarUrl.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String dob = etDateOfBirth.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Lấy giới tính từ RadioGroup
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        String gender = "";
        if (selectedGenderId == R.id.rbMale) {
            gender = "Nam";
        } else if (selectedGenderId == R.id.rbFemale) {
            gender = "Nữ";
        } else if (selectedGenderId == R.id.rbOther) {
            gender = "Khác";
        }

        // Kiểm tra các trường bắt buộc (ví dụ: tên)
        if (name.isEmpty()) {
            etName.setError("Tên không được để trống");
            etName.requestFocus();
            return;
        }

        // Tạo một đối tượng Map để lưu dữ liệu
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("dob", dob);
        userData.put("phone", phone);
        userData.put("address", address);
        userData.put("avatarUrl", avatarUrl);
        userData.put("gender", gender);
        // Luôn cập nhật cả email để đảm bảo nó được lưu cùng các thông tin khác
        if (mAuth.getCurrentUser() != null) {
            userData.put("email", mAuth.getCurrentUser().getEmail());
        }

        // Ghi dữ liệu vào Firestore
        // Sử dụng SetOptions.merge() để tạo mới nếu chưa có hoặc cập nhật nếu đã tồn tại
        db.collection("users").document(currentUserId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng Activity và quay lại màn hình trước đó (ProfileActivity)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi cập nhật hồ sơ", e);
                });
    }
}
