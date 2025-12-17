package com.example.projectmobile;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler; // Import cần thiết
import android.os.Looper; // Import cần thiết
import android.text.Editable; // Import cần thiết
import android.text.TextWatcher; // Import cần thiết
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmobile.model.Post;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivityDebug";

    private EditText etSearchQuery;
    private RecyclerView recyclerSearchResults;
    private TextView tvSearchResultCount;
    private ImageButton btnBack, btnDoSearch;

    private UserPostAdapter searchAdapter;
    private List<Post> searchList;

    // KHAI BÁO BIẾN MỚI CHO INSTANT SEARCH
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private final long SEARCH_DELAY = 500; // Độ trễ 500ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 1. Ánh xạ View
        btnBack = findViewById(R.id.btnBack);
        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnDoSearch = findViewById(R.id.btnDoSearch);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
        tvSearchResultCount = findViewById(R.id.tvSearchResultCount);

        // 2. Cấu hình RecyclerView
        searchList = new ArrayList<>();
        searchAdapter = new UserPostAdapter(searchList, this);
        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchResults.setAdapter(searchAdapter);

        // 3. Xử lý sự kiện Nút Quay lại
        btnBack.setOnClickListener(v -> finish());

        // 4. Xử lý sự kiện Tìm kiếm (Bấm nút) - GIỮ NGUYÊN NHƯNG SẼ HỦY VIỆC TÌM KIẾM TỰ ĐỘNG TRƯỚC
        btnDoSearch.setOnClickListener(v -> {
            handler.removeCallbacks(searchRunnable); // Hủy tìm kiếm tự động
            performSearch();
        });

        // 5. Xử lý sự kiện Tìm kiếm (Bấm Enter/Search trên bàn phím)
        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handler.removeCallbacks(searchRunnable); // Hủy tìm kiếm tự động
                performSearch();
                return true;
            }
            return false;
        });

        // 6. THÊM TEXT WATCHER CHO INSTANT SEARCH
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần làm gì
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Không cần làm gì
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Hủy bất kỳ tác vụ tìm kiếm nào đang chờ xử lý
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                // Định nghĩa tác vụ tìm kiếm mới
                searchRunnable = () -> performSearch();

                // Đặt tác vụ tìm kiếm mới vào hàng đợi với độ trễ (SEARCH_DELAY)
                handler.postDelayed(searchRunnable, SEARCH_DELAY);
            }
        });
        // KẾT THÚC INSTANT SEARCH SETUP
    }

    // --- HÀM THỰC HIỆN TÌM KIẾM (GIỮ NGUYÊN LOGIC) ---
    private void performSearch() {
        String query = etSearchQuery.getText().toString().trim();

        // Nếu query rỗng, xóa kết quả và đặt lại thông báo
        if (query.isEmpty()) {
            searchList.clear();
            searchAdapter.notifyDataSetChanged();
            tvSearchResultCount.setText("Hãy nhập từ khóa để tìm kiếm...");
            return;
        }

        // Ẩn bàn phím sau khi tìm kiếm thủ công (chỉ cho nút Search/Enter)
        // Lưu ý: Nếu muốn bàn phím không ẩn, hãy comment phần này lại.
        // InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // if (imm != null) {
        //     imm.hideSoftInputFromWindow(etSearchQuery.getWindowToken(), 0);
        // }

        // Bắt đầu truy vấn Firebase
        FirebaseFirestore.getInstance().collection("posts")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(snapshots -> {
                    searchList.clear();
                    String searchLower = query.toLowerCase();
                    int count = 0;

                    Log.d(TAG, "Tải thành công " + snapshots.size() + " bài viết đã duyệt.");

                    for (DocumentSnapshot doc : snapshots) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            String title = post.getTitle() != null ? post.getTitle().toLowerCase() : "";
                            String content = post.getContent() != null ? post.getContent().toLowerCase() : "";

                            Log.d(TAG, "Kiểm tra Bài: " + title + " | Query: " + searchLower);

                            // Kiểm tra nếu tiêu đề HOẶC nội dung chứa từ khóa
                            if (title.contains(searchLower) || content.contains(searchLower)) {
                                searchList.add(post);
                                count++;
                                Log.d(TAG, "=> KHỚP, thêm vào danh sách kết quả.");
                            }
                        }
                    }

                    searchAdapter.notifyDataSetChanged();
                    tvSearchResultCount.setText("Tìm thấy " + count + " kết quả cho \"" + query + "\"");

                    if (count == 0) {
                        // Không cần Toast ở đây, vì trạng thái đã hiển thị trên TextView
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi tải dữ liệu tìm kiếm: ", e);
                    Toast.makeText(SearchActivity.this, "Lỗi tìm kiếm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvSearchResultCount.setText("Lỗi tải dữ liệu.");
                });
    }
}