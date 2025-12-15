package com.example.projectmobile;



import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmobile.model.User;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    List<User> userList;
    Context context;

    public AdminUserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvEmail.setText(user.getEmail());
        // Kiểm tra null để tránh lỗi crash nếu UID bị thiếu
        holder.tvUid.setText("UID: " + (user.getUid() != null ? user.getUid() : "N/A"));

        // Lấy background hiện tại
        android.graphics.drawable.Drawable background = holder.tvRole.getBackground();

        if ("admin".equals(user.getRole())) {
            holder.tvRole.setText("ADMIN");
            // Đổi màu đỏ
            if (background instanceof GradientDrawable) {
                // .mutate() để đảm bảo chỉ đổi màu dòng này, không ảnh hưởng dòng khác
                ((GradientDrawable) background.mutate()).setColor(Color.parseColor("#D32F2F"));
            }
        } else {
            holder.tvRole.setText("USER");
            // Đổi màu xanh
            if (background instanceof GradientDrawable) {
                ((GradientDrawable) background.mutate()).setColor(Color.parseColor("#1976D2"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvRole, tvUid;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            tvUid = itemView.findViewById(R.id.tvUserUid);
        }
    }
}