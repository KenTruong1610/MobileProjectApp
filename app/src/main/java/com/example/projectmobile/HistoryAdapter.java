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

// Adapter này gần giống hệt UserPostAdapter nhưng không có logic lưu lại lịch sử
// để tránh vòng lặp vô hạn khi người dùng nhấn vào một bài trong trang lịch sử.
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    List<Post> list;
    Context context;

    public HistoryAdapter(List<Post> list, Context context) {
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
        Post post = list.get(position);

        holder.tvTitle.setText(post.getTitle());
        if(post.getTimestamp() != null) {
            holder.tvDate.setText(post.getTimestamp().toString());
        }
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(context).load(post.getImageUrl()).into(holder.imgThumb);
        }

        holder.itemView.setOnClickListener(v -> {
            // Chỉ chuyển màn hình, không lưu lại lịch sử
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("post", post);
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
