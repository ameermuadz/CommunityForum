package com.example.communityforum;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private Context context;
    private String currentUserId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.textPostTitle.setText(post.getTitle());
        holder.textPostContent.setText(post.getContent());

        // Get the user ID from the post
        String posterId = post.getPosterId();

        // Use UserManager to get the user and set the poster name
        if (posterId != null) {
            User user = UserManager.getUserById(posterId);
            if (user != null) {
                holder.textPostPosterName.setText(user.getUserName());
            } else {
                holder.textPostPosterName.setText("Unknown User");
            }
        } else {
            holder.textPostPosterName.setText("Unknown User");
        }

        holder.userProfileImage.setImageResource(R.drawable.default_user_image);
        holder.textUpvoteCount.setText(String.valueOf(post.getUpvotes()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        if (post.getTimestamp() != null) {
            String formattedTimestamp = sdf.format(post.getTimestamp());
            holder.textPostTimestamp.setText(formattedTimestamp);
        } else {
            holder.textPostTimestamp.setText("No timestamp");
        }

        // Set up a snapshot listener for the comments collection
        ListenerRegistration listenerRegistration = db.collection("posts").document(post.getPostId()).collection("comments")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("PostAdapter", "Error listening to comments", e);
                        return;
                    }
                    if (querySnapshot != null) {
                        int commentCount = querySnapshot.size();
                        holder.textCommentCount.setText(commentCount + " Comments");
                    }
                });

        // Store the listener registration in the ViewHolder
        holder.listenerRegistration = listenerRegistration;

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context,
                    PostDetailsActivity.class);
            intent.putExtra("postId",
                    post.getPostId());
            ((MainActivity) context)
                    .startActivityForResult(
                            intent,
                            1);
        });

        currentUserId = UserManager.getCurrentUser().getUserId();

        // Get the user's vote from Firestore and set the button colors
        VoteUtils.getUserVote(post.getPostId(), currentUserId, voteType -> {
            boolean isUpvoted = voteType.equals("upvote");
            boolean isDownvoted = voteType.equals("downvote");

            holder.btnUpvote.setSelected(isUpvoted);
            holder.btnDownvote.setSelected(isDownvoted);

            holder.btnUpvote.setCompoundDrawableTintList(ContextCompat.getColorStateList(context, R.color.upvote_button_color));
            holder.btnDownvote.setCompoundDrawableTintList(ContextCompat.getColorStateList(context, R.color.downvote_button_color));
        });

        holder.btnUpvote.setOnClickListener(view -> {
            Log.d("PostAdapter", "Upvote button clicked for post: " + post.getPostId());
            VoteUtils.handleVote(post.getPostId(), "upvote", holder.textUpvoteCount, currentUserId);
            boolean currentUpvoteState = holder.btnUpvote.isSelected();
            holder.btnUpvote.setSelected(!currentUpvoteState);
            holder.btnUpvote.setCompoundDrawableTintList(ContextCompat.getColorStateList(context, R.color.upvote_button_color));
            holder.btnDownvote.setSelected(false);
            holder.btnDownvote.setCompoundDrawableTintList(ContextCompat.getColorStateList(context, R.color.downvote_button_color));
        });

        holder.btnDownvote.setOnClickListener(view -> {
            Log.d("PostAdapter", "Downvote button clicked for post: " + post.getPostId());
            VoteUtils.handleVote(post.getPostId(), "downvote", holder.textUpvoteCount, currentUserId);
            boolean currentDownvoteState = holder.btnDownvote.isSelected();
            holder.btnDownvote.setSelected(!currentDownvoteState);
            holder.btnDownvote.setCompoundDrawableTintList(ContextCompat.getColorStateList(context, R.color.downvote_button_color));
            holder.btnUpvote.setSelected(false);
            holder.btnUpvote.setCompoundDrawableTintList(ContextCompat.getColorStateList(context, R.color.upvote_button_color));
        });

        // Set up bookmark button
        // Inside onBindViewHolder
        BookmarkUtils.isBookmarked(post.getPostId(), currentUserId, new BookmarkUtils.BookmarkCheckCallback() {
            @Override
            public void onBookmarkChecked(boolean isBookmarked) {
                holder.btnBookmark.setSelected(isBookmarked);
                holder.btnBookmark.setCompoundDrawableTintList(ContextCompat.getColorStateList(context, R.color.bookmark_button_color));
            }
        });

        holder.btnBookmark.setOnClickListener(view -> {
            boolean isCurrentlyBookmarked = holder.btnBookmark.isSelected();
            if (isCurrentlyBookmarked) {
                BookmarkUtils.removeBookmark(post.getPostId(), currentUserId, new BookmarkUtils.BookmarkCallback() {
                    @Override
                    public void onBookmarkSaved(boolean isSaved) {
                        // Do nothing
                    }

                    @Override
                    public void onBookmarkRemoved(boolean isRemoved) {
                        if (isRemoved) {
                            holder.btnBookmark.setSelected(false);
                            holder.btnBookmark.setCompoundDrawableTintList(ContextCompat.getColorStateList(context, R.color.bookmark_button_color));
                        }
                    }
                });
            } else {
                BookmarkUtils.saveBookmark(post.getPostId(), currentUserId, new BookmarkUtils.BookmarkCallback() {
                    @Override
                    public void onBookmarkSaved(boolean isSaved) {
                        if (isSaved) {
                            holder.btnBookmark.setSelected(true);
                            holder.btnBookmark.setCompoundDrawableTintList(ContextCompat.getColorStateList(context, R.color.bookmark_button_color));
                        }
                    }

                    @Override
                    public void onBookmarkRemoved(boolean isRemoved) {
                        // Do nothing
                    }
                });
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull PostViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.listenerRegistration != null) {
            holder.listenerRegistration.remove();
            holder.listenerRegistration = null;
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textPostTitle, textPostContent, textPostPosterName, textUpvoteCount, textPostTimestamp, textCommentCount;
        ImageView userProfileImage;
        Button btnUpvote, btnDownvote, btnBookmark;
        ListenerRegistration listenerRegistration;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textPostTitle = itemView.findViewById(R.id.textPostTitle);
            textPostContent = itemView.findViewById(R.id.textPostContent);
            textPostPosterName = itemView.findViewById(R.id.textPostPosterName);
            textUpvoteCount = itemView.findViewById(R.id.textUpvoteCount);
            textPostTimestamp = itemView.findViewById(R.id.textPostTimestamp);
            textCommentCount = itemView.findViewById(R.id.textCommentCount);
            btnUpvote = itemView.findViewById(R.id.btnUpvote);
            btnDownvote = itemView.findViewById(R.id.btnDownvote);
            btnBookmark = itemView.findViewById(R.id.btnBookmark);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
        }
    }
}