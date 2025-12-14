package com.example.projectmobile;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmobile.model.User; // Import class User vừa tạo

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

        // Trang trí màu sắc dựa trên Role
        if ("admin".equals(user.getRole())) {
            holder.tvRole.setText("QUẢN TRỊ VIÊN (Admin)");
            holder.tvRole.setTextColor(Color.RED);
        } else {
            holder.tvRole.setText("Người dùng (User)");
            holder.tvRole.setTextColor(Color.BLUE);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
        }
    }
}