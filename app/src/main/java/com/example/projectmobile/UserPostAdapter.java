package com.example.projectmobile;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.projectmobile.model.Post;
import java.util.List;

public class UserPostAdapter extends RecyclerView.Adapter<UserPostAdapter.ViewHolder> {

    List<Post> list;
    Context context;

    public UserPostAdapter(List<Post> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_user, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 1. Lấy bài viết tại vị trí hiện tại
        Post post = list.get(position);

        // 2. Gán dữ liệu lên giao diện (như cũ)
        holder.tvTitle.setText(post.getTitle());
        if(post.getTimestamp() != null) {
            holder.tvDate.setText(post.getTimestamp().toString());
        }
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(context).load(post.getImageUrl()).into(holder.imgThumb);
        }

        // 3. XỬ LÝ SỰ KIỆN CLICK (ĐOẠN BẠN CẦN THÊM)
        holder.itemView.setOnClickListener(v -> {
            // Tạo Intent để chuyển sang màn hình PostDetailActivity
            Intent intent = new Intent(context, PostDetailActivity.class);

            // Đóng gói object "post" gửi đi (Post phải implements Serializable)
            intent.putExtra("post", post);

            // Bắt đầu chuyển màn hình
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        ImageView imgThumb;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPostTitle);
            tvDate = itemView.findViewById(R.id.tvPostDate);
            imgThumb = itemView.findViewById(R.id.imgPostThumb);
        }
    }
}