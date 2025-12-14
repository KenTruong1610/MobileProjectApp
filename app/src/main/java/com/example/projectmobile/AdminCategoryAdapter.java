package com.example.projectmobile;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmobile.model.Category; // Import class Category
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {

    private List<Category> categoryList;
    private Context context;
    private FirebaseFirestore db;

    // Constructor để truyền dữ liệu từ Activity vào Adapter
    public AdminCategoryAdapter(List<Category> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Gọi file giao diện item_category_admin.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Lấy danh mục tại vị trí hiện tại
        Category cat = categoryList.get(position);

        // Hiển thị tên danh mục
        holder.tvName.setText(cat.getName());

        // Xử lý sự kiện bấm nút Xóa
        holder.btnDelete.setOnClickListener(v -> {
            // Xóa trên Firestore
            db.collection("categories").document(cat.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Đã xóa danh mục!", Toast.LENGTH_SHORT).show();

                        // Cập nhật giao diện ngay lập tức (Xóa dòng đó khỏi list)
                        categoryList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, categoryList.size());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        if (categoryList != null) {
            return categoryList.size();
        }
        return 0;
    }

    // Class ViewHolder để ánh xạ các view trong file xml
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCatName);
            btnDelete = itemView.findViewById(R.id.btnDeleteCat);
        }
    }
}