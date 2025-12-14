package com.example.projectmobile;



import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmobile.model.Post;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminPostAdapter extends RecyclerView.Adapter<AdminPostAdapter.ViewHolder> {

    List<Post> postList;
    Context context;
    FirebaseFirestore db;

    public AdminPostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.title.setText(post.getTitle());

        if (post.getStatus().equals("approved")) {
            holder.status.setText("Đã duyệt");
            holder.status.setTextColor(Color.GREEN);
            holder.btnApprove.setEnabled(false); // Đã duyệt thì không bấm nữa
            holder.btnApprove.setText("Đã duyệt");
        } else {
            holder.status.setText("Chờ duyệt");
            holder.status.setTextColor(Color.parseColor("#FF9800")); // Màu cam
            holder.btnApprove.setEnabled(true);
            holder.btnApprove.setText("Duyệt bài");
        }

        // Sự kiện Duyệt bài
        holder.btnApprove.setOnClickListener(v -> {
            db.collection("posts").document(post.getId())
                    .update("status", "approved")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Đã duyệt bài!", Toast.LENGTH_SHORT).show();
                        notifyItemChanged(position);
                    });
        });

        // Sự kiện Xóa bài
        holder.btnDelete.setOnClickListener(v -> {
            db.collection("posts").document(post.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Đã xóa bài!", Toast.LENGTH_SHORT).show();
                        postList.remove(position);
                        notifyItemRemoved(position);
                    });
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, status;
        Button btnApprove, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvPostTitle);
            status = itemView.findViewById(R.id.tvPostStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}