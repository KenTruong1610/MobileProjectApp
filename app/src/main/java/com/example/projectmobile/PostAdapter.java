package com.example.projectmobile;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmobile.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private static final String TAG = "PostAdapter";
    private Context context;
    private List<Post> postList;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_related_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvTitle.setText(post.getTitle());
        if (post.getTimestamp() != null) {
            CharSequence dateStr = DateFormat.format("dd/MM/yyyy", post.getTimestamp());
            holder.tvDate.setText(dateStr);
        }

        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(context).load(post.getImageUrl()).into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_background); // A default image
        }

        holder.itemView.setOnClickListener(v -> {
            // Save to history
            saveToHistory(post.getId());

            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("post", post);
            context.startActivity(intent);
        });
    }

    private void saveToHistory(String postId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || postId == null) {
            return; // Not logged in or no post ID, do nothing
        }
        String userId = currentUser.getUid();

        Map<String, Object> historyData = new HashMap<>();
        historyData.put("postId", postId);
        historyData.put("readAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("history")
                .document(postId) // Use postId as document ID to automatically overwrite
                .set(historyData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Saved to history: " + postId))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving to history", e));
    }

    @Override
    public int getItemCount() {
        if (postList != null) {
            return postList.size();
        }
        return 0;
    }

    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivPostImage);
            tvTitle = itemView.findViewById(R.id.tvPostTitle);
            tvDate = itemView.findViewById(R.id.tvPostDate);
        }
    }
}
