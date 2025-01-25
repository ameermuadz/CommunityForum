package com.example.communityforum;

import android.util.Log;
import android.widget.TextView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class VoteUtils {

    public static void handleVote(String postId, String voteType, TextView textUpvoteCount, String currentUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("posts").document(postId);
        Log.d("VoteUtils", "handleVote: postId = " + postId + ", voteType = " + voteType + ", currentUserId = " + currentUserId);

        final TextView upvoteCountTextView = textUpvoteCount;

        db.runTransaction(transaction -> {
            DocumentReference voteRef = postRef.collection("votes").document(currentUserId);
            DocumentReference postRefTransaction = transaction.get(postRef).getReference();
            int upvotes = 0;
            String currentUserVote = "";
            if (transaction.get(postRefTransaction).exists()) {
                Post post = transaction.get(postRefTransaction).toObject(Post.class);
                if (post != null) {
                    upvotes = post.getUpvotes();
                }
            }
            if (transaction.get(voteRef).exists()) {
                currentUserVote = transaction.get(voteRef).getString("voteType");
            }
            if (voteType.equals("upvote")) {
                if (currentUserVote.equals("upvote")) {
                    upvotes--;
                    transaction.delete(voteRef);
                } else if (currentUserVote.equals("downvote")) {
                    upvotes += 2;
                    Map<String, Object> vote = new HashMap<>();
                    vote.put("voteType", "upvote");
                    transaction.set(voteRef, vote);
                } else {
                    upvotes++;
                    Map<String, Object> vote = new HashMap<>();
                    vote.put("voteType", "upvote");
                    transaction.set(voteRef, vote);
                }
            } else if (voteType.equals("downvote")) {
                if (currentUserVote.equals("downvote")) {
                    upvotes++;
                    transaction.delete(voteRef);
                } else if (currentUserVote.equals("upvote")) {
                    upvotes -= 2;
                    Map<String, Object> vote = new HashMap<>();
                    vote.put("voteType", "downvote");
                    transaction.set(voteRef, vote);
                } else {
                    upvotes--;
                    Map<String, Object> vote = new HashMap<>();
                    vote.put("voteType", "downvote");
                    transaction.set(voteRef, vote);
                }
            }
            transaction.update(postRef, "upvotes", upvotes);
            return upvotes;
        }).addOnSuccessListener(result -> {
            upvoteCountTextView.setText(String.valueOf(result));
        }).addOnFailureListener(e -> {
            Log.e("VoteUtils", "Error updating vote", e);
        });
    }

    public static void getUserVote(String postId, String currentUserId, VoteCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference voteRef = db.collection("posts").document(postId).collection("votes").document(currentUserId);

        voteRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String voteType = documentSnapshot.getString("voteType");
                callback.onVoteTypeReceived(voteType != null ? voteType : "");
            } else {
                callback.onVoteTypeReceived(""); // No vote found
            }
        }).addOnFailureListener(e -> {
            Log.e("VoteUtils", "Error getting user vote", e);
            callback.onVoteTypeReceived(""); // Handle error case
        });
    }

    public interface VoteCallback {
        void onVoteTypeReceived(String voteType);
    }
}