package com.example.communityforum;

import android.util.Log;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookmarkUtils {

    public interface BookmarkCallback {
        void onBookmarkSaved(boolean isSaved);
        void onBookmarkRemoved(boolean isRemoved);
    }

    public interface BookmarksCallback {
        void onBookmarksReceived(List<String> bookmarkedIds);
    }

    public interface BookmarkCheckCallback {
        void onBookmarkChecked(boolean isBookmarked);
    }

    public static void saveBookmark(String postId, String currentUserId, BookmarkCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference bookmarkRef = db.collection("BookmarkedForumPost").document(currentUserId).collection("bookmarks").document(postId);

        Map<String, Object> bookmarkData = new HashMap<>();
        bookmarkData.put("bookmarked", true);

        bookmarkRef.set(bookmarkData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("BookmarkUtils", "Bookmark saved for post: " + postId);
                    callback.onBookmarkSaved(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("BookmarkUtils", "Error saving bookmark for post: " + postId, e);
                    callback.onBookmarkSaved(false);
                });
    }

    public static void removeBookmark(String postId, String currentUserId, BookmarkCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference bookmarkRef = db.collection("BookmarkedForumPost").document(currentUserId).collection("bookmarks").document(postId);

        bookmarkRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("BookmarkUtils", "Bookmark removed for post: " + postId);
                    callback.onBookmarkRemoved(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("BookmarkUtils", "Error removing bookmark for post: " + postId, e);
                    callback.onBookmarkRemoved(false);
                });
    }

    public static void getBookmarkedPostIds(String currentUserId, BookmarksCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("BookmarkedForumPost").document(currentUserId).collection("bookmarks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> bookmarkedIds = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        bookmarkedIds.add(document.getId());
                    }
                    callback.onBookmarksReceived(bookmarkedIds);
                })
                .addOnFailureListener(e -> {
                    Log.e("BookmarkUtils", "Error getting bookmarked post IDs", e);
                    callback.onBookmarksReceived(new ArrayList<>());
                });
    }

    public static void isBookmarked(String postId, String currentUserId, BookmarkCheckCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("BookmarkedForumPost").document(currentUserId).collection("bookmarks").document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isBookmarked = documentSnapshot.exists();
                    callback.onBookmarkChecked(isBookmarked);
                })
                .addOnFailureListener(e -> {
                    Log.e("BookmarkUtils", "Error checking bookmark for post: " + postId, e);
                    callback.onBookmarkChecked(false);
                });
    }
}