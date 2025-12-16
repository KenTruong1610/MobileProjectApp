package com.example.projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnGoogle;
    TextView tvRegisterLink;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Ánh xạ View
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogleLogin); // Nút đăng nhập Google
        tvRegisterLink = findViewById(R.id.tvGoToRegister);

        // 2. Khởi tạo Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // 3. Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Tự động lấy từ google-services.json
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // --- SỰ KIỆN CLICK ---

        // Bấm nút Google
        // Bấm nút Google
        btnGoogle.setOnClickListener(v -> {
            // Bước 1: Đăng xuất tài khoản Google hiện tại (để xóa cache)
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // Bước 2: Sau khi đăng xuất xong thì mới mở bảng chọn tài khoản
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });

        // Bấm nút Đăng nhập thường
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email không được để trống");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Mật khẩu không được để trống");
                return;
            }

            fAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                checkUserRole(authResult.getUser().getUid());
            }).addOnFailureListener(e -> {
                Toast.makeText(LoginActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        });
    }

    // --- PHẦN XỬ LÝ GOOGLE ---

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        // Đã lấy được tài khoản Google -> Xác thực với Firebase
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, "Lỗi Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = fAuth.getCurrentUser();
                        // QUAN TRỌNG: Lưu thông tin vào Firestore ngay
                        saveGoogleUserToFirestore(user);
                    } else {
                        Toast.makeText(this, "Xác thực thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Hàm này sẽ tạo User trong database nếu chưa có
    private void saveGoogleUserToFirestore(FirebaseUser user) {
        String uid = user.getUid();
        String email = user.getEmail();

        DocumentReference docRef = fStore.collection("users").document(uid);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                // Nếu User chưa tồn tại trong bảng 'users', tạo mới
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("email", email);
                userMap.put("role", "user"); // Mặc định là User thường

                docRef.set(userMap).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã tạo tài khoản mới!", Toast.LENGTH_SHORT).show();
                    checkUserRole(uid); // Chuyển trang sau khi tạo xong
                });
            } else {
                // Nếu User đã tồn tại, kiểm tra quyền luôn
                checkUserRole(uid);
            }
        });
    }

    // --- KIỂM TRA QUYỀN VÀ CHUYỂN TRANG ---

    private void checkUserRole(String uid) {
        DocumentReference df = fStore.collection("users").document(uid);
        df.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                if ("admin".equals(role)) {
                    startActivity(new Intent(getApplicationContext(), AdminActivity.class));
                    finish();
                } else {
                    // Mặc định chuyển sang UserActivity
                    startActivity(new Intent(getApplicationContext(), UserActivity.class));
                    finish();
                }
            } else {
                // Trường hợp hy hữu: Đăng nhập được nhưng không có dữ liệu
                // Thử tạo lại dữ liệu
                if(fAuth.getCurrentUser() != null) {
                    saveGoogleUserToFirestore(fAuth.getCurrentUser());
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (fAuth.getCurrentUser() != null) {
            checkUserRole(fAuth.getCurrentUser().getUid());
        }
    }
}