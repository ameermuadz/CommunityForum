package com.example.communityforum;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> commentList;
    private Context context;

    public CommentAdapter(List<Comment> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
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
        holder.content.setText(comment.getContent());

        // Get the user ID from the comment
        String posterId = comment.getPosterId();
        Log.d("CommentAdapter", "Binding comment - posterId: " + posterId); // Log the posterId

        // Use UserManager to get the user and set the poster name
        if (posterId != null) {
            User user = UserManager.getUserById(posterId); // Get user by ID
            if (user != null) {
                holder.posterName.setText(user.getUserName());
                Log.d("CommentAdapter", "User found - userName: " + user.getUserName()); // Log the user name
            } else {
                holder.posterName.setText("Unknown User");
                Log.d("CommentAdapter", "User not found for posterId: " + posterId); // Log if user not found
            }
        } else {
            holder.posterName.setText("Unknown User");
            Log.d("CommentAdapter", "posterId is null"); // Log if posterId is null
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        if (comment.getTimestamp() != null) {
            String formattedTimestamp = sdf.format(comment.getTimestamp());
            holder.timestamp.setText(formattedTimestamp); // Set formatted timestamp
        } else {
            holder.timestamp.setText("No timestamp");
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView content, posterName, timestamp;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.textCommentContent);
            posterName = itemView.findViewById(R.id.textCommentPosterName);
            timestamp = itemView.findViewById(R.id.textCommentTimestamp);
        }
    }
}
