package com.example.projectmobile;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmobile.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> commentList;
    private String currentUserEmail;
    private OnCommentDeleteListener deleteListener;

    public interface OnCommentDeleteListener {
        void onCommentDelete(String commentId);
    }

    public CommentAdapter(Context context, List<Comment> commentList, OnCommentDeleteListener deleteListener) {
        this.context = context;
        this.commentList = commentList;
        this.deleteListener = deleteListener;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            this.currentUserEmail = currentUser.getEmail();
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.tvUserEmail.setText(comment.getUserEmail());
        holder.tvContent.setText(comment.getContent());
        if (comment.getTimestamp() != null) {
            CharSequence dateStr = DateFormat.format("dd/MM/yyyy HH:mm", comment.getTimestamp());
            holder.tvDate.setText(dateStr);
        }

        if (currentUserEmail != null && currentUserEmail.equals(comment.getUserEmail())) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onCommentDelete(comment.getId());
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void removeComment(String commentId) {
        int position = -1;
        for (int i = 0; i < commentList.size(); i++) {
            if (commentList.get(i).getId().equals(commentId)) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            commentList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, commentList.size());
        }
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserEmail, tvContent, tvDate;
        ImageView btnDelete;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvCommentUserEmail);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvDate = itemView.findViewById(R.id.tvCommentDate);
            btnDelete = itemView.findViewById(R.id.btnDeleteComment);
        }
    }
}
