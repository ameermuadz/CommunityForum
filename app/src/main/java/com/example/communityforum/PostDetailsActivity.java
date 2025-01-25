package com.example.communityforum;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class PostDetailsActivity extends AppCompatActivity {

    private RecyclerView commentsRecyclerView;
    private EditText inputComment;
    private Button btnSubmitComment;
    private List<Comment> commentList;
    private CommentAdapter commentAdapter;
    private FirebaseFirestore db;
    private String postId;

    private TextView textPostTitle, textPostContent, textPostPosterName, textUpvoteCount, textPostTimestamp, textCommentCount;
    private Button btnUpvote, btnDownvote, btnBookmark;
    private Post currentPost; // Add this to store the current post
    private boolean postDetailsLoaded = false; // Flag to track if post details are loaded
    private ListenerRegistration commentsListener;
    private Button btnBack;

    public interface OnBookmarkChangedListener {
        void onBookmarkChanged();
    }
    private OnBookmarkChangedListener onBookmarkChangedListener;
    public void setOnBookmarkChangedListener(OnBookmarkChangedListener listener) {
        this.onBookmarkChangedListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        Log.d("PostDetailsActivity", "onCreate: Activity created");

        commentsRecyclerView = findViewById(R.id.recyclerViewComments);
        inputComment = findViewById(R.id.inputComment);
        btnSubmitComment = findViewById(R.id.btnSubmitComment);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList, this);
        commentsRecyclerView.setAdapter(commentAdapter);
        View postDetailsTop = findViewById(R.id.postDetailsTop);
        textPostTitle = postDetailsTop.findViewById(R.id.textPostTitle);
        textPostContent = postDetailsTop.findViewById(R.id.textPostContent);
        textPostPosterName = postDetailsTop.findViewById(R.id.textPostPosterName);
        textUpvoteCount = postDetailsTop.findViewById(R.id.textUpvoteCount);
        textPostTimestamp = postDetailsTop.findViewById(R.id.textPostTimestamp);
        textCommentCount = postDetailsTop.findViewById(R.id.textCommentCount);
        btnUpvote = postDetailsTop.findViewById(R.id.btnUpvote);
        btnDownvote = postDetailsTop.findViewById(R.id.btnDownvote);
        btnBookmark = postDetailsTop.findViewById(R.id.btnBookmark);

        db = FirebaseFirestore.getInstance();

        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Log.e("PostDetailsActivity", "postId is null, exiting activity");
            finish();
            return;
        }

        // Disable vote buttons initially
        btnUpvote.setEnabled(false);
        btnDownvote.setEnabled(false);

        loadPostDetails();
        loadComments();

        btnSubmitComment.setOnClickListener(v -> addComment());
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());

        String currentUserId = UserManager.getCurrentUser().getUserId();

        // Set up bookmark button
        BookmarkUtils.isBookmarked(postId, currentUserId, isBookmarked -> {
            btnBookmark.setSelected(isBookmarked);
            btnBookmark.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.bookmark_button_color));
        });
        btnBookmark.setOnClickListener(view -> {
            boolean isCurrentlyBookmarked = btnBookmark.isSelected();
            if (isCurrentlyBookmarked) {
                BookmarkUtils.removeBookmark(postId, currentUserId, new BookmarkUtils.BookmarkCallback() {
                    @Override
                    public void onBookmarkSaved(boolean isSaved) {
                        // This method is not used in removeBookmark, so we can leave it empty
                    }

                    @Override
                    public void onBookmarkRemoved(boolean isRemoved) {
                        if (isRemoved) {
                            btnBookmark.setSelected(false);
                            btnBookmark.setCompoundDrawableTintList(ContextCompat.getColorStateList(PostDetailsActivity.this, R.color.bookmark_button_color));
                            updateBookmarkStatus(false);
                        }
                    }
                });
            } else {
                BookmarkUtils.saveBookmark(postId, currentUserId, new BookmarkUtils.BookmarkCallback() {
                    @Override
                    public void onBookmarkSaved(boolean isSaved) {
                        if (isSaved) {
                            btnBookmark.setSelected(true);
                            btnBookmark.setCompoundDrawableTintList(ContextCompat.getColorStateList(PostDetailsActivity.this, R.color.bookmark_button_color));
                            updateBookmarkStatus(true);
                        }
                    }

                    @Override
                    public void onBookmarkRemoved(boolean isRemoved) {
                        // This method is not used in saveBookmark, so we can leave it empty
                    }
                });
            }
        });

        // Set up initial button colors based on user's vote
        VoteUtils.getUserVote(postId, currentUserId, voteType -> {
            boolean isUpvoted = voteType.equals("upvote");
            boolean isDownvoted = voteType.equals("downvote");

            btnUpvote.setSelected(isUpvoted);
            btnDownvote.setSelected(isDownvoted);

            btnUpvote.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.upvote_button_color));
            btnDownvote.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.downvote_button_color));
        });

        btnUpvote.setOnClickListener(v -> {
            Log.d("PostDetailsActivity", "Upvote button clicked, postId = " + postId);
            if (postDetailsLoaded) {
                VoteUtils.handleVote(postId, "upvote", textUpvoteCount, currentUserId);
                btnUpvote.setSelected(!btnUpvote.isSelected());
                btnUpvote.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.upvote_button_color));
                btnDownvote.setSelected(false);
                btnDownvote.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.downvote_button_color));
            } else {
                Log.e("PostDetailsActivity", "Post details not loaded, cannot vote");
            }
        });

        btnDownvote.setOnClickListener(v -> {
            Log.d("PostDetailsActivity", "Downvote button clicked, postId = " + postId);
            if (postDetailsLoaded) {
                VoteUtils.handleVote(postId, "downvote", textUpvoteCount, currentUserId);
                btnDownvote.setSelected(!btnDownvote.isSelected());
                btnDownvote.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.downvote_button_color));
                btnUpvote.setSelected(false);
                btnUpvote.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.upvote_button_color));
            } else {
                Log.e("PostDetailsActivity", "Post details not loaded, cannot vote");
            }
        });
        Intent resultIntent = new Intent();
        resultIntent.putExtra("bookmarkChanged", true); // Send a flag
        setResult(RESULT_OK, resultIntent);
    }

    private void updateBookmarkStatus(boolean isBookmarked) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("postId", postId);
        resultIntent.putExtra("isBookmarked", isBookmarked);
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    public void onBackPressed() {
        // Set the result before finishing the activity
        updateBookmarkStatus(btnBookmark.isSelected());
        super.onBackPressed();
    }

    private void loadPostDetails() {
        if (postId != null) {
            db.collection("posts")
                    .document(postId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentPost = documentSnapshot
                                    .toObject(Post.class);
                            if (currentPost != null) {
                                textPostTitle
                                        .setText(currentPost
                                                .getTitle());
                                textPostContent
                                        .setText(currentPost
                                                .getContent());
                                String posterId = currentPost
                                        .getPosterId();
                                if (posterId != null) {
                                    User user = UserManager
                                            .getUserById(posterId);
                                    if (user != null) {
                                        textPostPosterName
                                                .setText(user
                                                        .getUserName());
                                    } else {
                                        textPostPosterName
                                                .setText("Unknown User");
                                    }
                                } else {
                                    textPostPosterName
                                            .setText("Unknown User");
                                }
                                textUpvoteCount.setText(String
                                        .valueOf(currentPost.getUpvotes()));
                                SimpleDateFormat sdf
                                        = new SimpleDateFormat(
                                                "yyyy-MM-dd HH:mm:ss",
                                        Locale.getDefault());
                                sdf.setTimeZone(TimeZone.getTimeZone(
                                        "Asia/Kuala_Lumpur"));
                                if (currentPost.getTimestamp() != null) {
                                    String formattedTimestamp
                                            = sdf.format(currentPost
                                            .getTimestamp());
                                    textPostTimestamp.setText(
                                            formattedTimestamp);
                                } else {
                                    textPostTimestamp.setText("No timestamp");
                                }
                                // Set up a snapshot listener for the comments collection
                                commentsListener = db
                                        .collection("posts")
                                        .document(postId)
                                        .collection("comments")
                                        .addSnapshotListener((querySnapshot, e) -> {
                                            if (e != null) {
                                                Log.e("PostDetailsActivity",
                                                        "Error listening to comments"
                                                        , e);
                                                return;
                                            }
                                            if (querySnapshot != null) {
                                                int commentCount
                                                        = querySnapshot.size();
                                                textCommentCount
                                                        .setText(commentCount
                                                                + " Comments");
                                            }
                                        });
                                // Enable vote buttons after currentPost is initialized
                                btnUpvote.setEnabled(true);
                                btnDownvote.setEnabled(true);
                                postDetailsLoaded = true; // Set the flag to true
                            }
                        } else {
                            Log.e("PostDetailsActivity"
                                    , "Post document not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("PostDetailsActivity"
                                , "Error loading post details", e);
                    });
        }
    }

    private void loadComments() {
        db.collection("posts")
                .document(postId)
                .collection("comments")
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {
                    commentList.clear();
                    for (QueryDocumentSnapshot
                            document :
                            queryDocumentSnapshots) {
                        Date timestamp
                                = document
                                .getDate("timestamp");
                        String posterId
                                = document
                                .getString("posterId");
                        Log.d("PostDetailsActivity"
                                , "Loaded comment - posterId: "
                                        + posterId);
                        Comment comment
                                = new Comment(
                                document.getId(),
                                document.getString("content"),
                                posterId,
                                timestamp
                        );
                        commentList.add(comment);
                    }
                    commentAdapter.notifyDataSetChanged();
                });
    }

    private void addComment() {
        String commentText = inputComment
                .getText().toString().trim();
        if (commentText.isEmpty()) return;

        String commentId = db
                .collection("comments")
                .document().getId();
        Date timestamp = new Date();

        User currentUser
                = UserManager.getCurrentUser();
        String posterId
                = (currentUser != null)
                ? currentUser.getUserId() : null;

        Map<String, Object> comment = new HashMap<>();
        comment.put("commentId", commentId);
        comment.put("content", commentText);
        comment.put("posterId", posterId);
        comment.put("timestamp", timestamp);

        db.collection("posts")
                .document(postId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(documentReference -> {
                    inputComment.setText("");
                    loadComments();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentsListener != null) {
            commentsListener.remove();
            commentsListener = null;
        }
    }
}