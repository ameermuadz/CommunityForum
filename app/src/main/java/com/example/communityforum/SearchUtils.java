package com.example.communityforum;

import java.util.ArrayList;
import java.util.List;

public class SearchUtils {

    public interface SearchCallback {
        void onSearchResults(List<Post> searchResults);
    }

    public static void searchPosts(String query
            , List<Post> allPosts, SearchCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onSearchResults(allPosts);
            return;
        }

        List<Post> searchResults = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        for (Post post : allPosts) {
            if (post.getTitle().toLowerCase()
                    .contains(lowerCaseQuery) ||
                    post.getContent().toLowerCase()
                            .contains(lowerCaseQuery) ||
                    post.getPosterId().toLowerCase()
                            .contains(lowerCaseQuery)) {
                searchResults.add(post);
            }
        }
        callback.onSearchResults(searchResults);
    }
}