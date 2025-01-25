package com.example.communityforum;


import java.util.Date;

public class Post {
    private String postId;
    private String title;
    private String content;
    private boolean anonymous;
    private int upvotes;
    private String posterId;
    private String currentUserVote;
    private Date timestamp;
    private int commentCount;
    private boolean isBookmarked;
    public Post() {}

    public Post(String postId, String title, String content, boolean anonymous, int upvotes, Date timestamp, String posterId, int commentCount) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.anonymous = anonymous;
        this.upvotes = upvotes;
        this.currentUserVote = "";
        this.timestamp = timestamp;
        this.commentCount = commentCount;
        this.posterId = posterId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public String getCurrentUserVote() {
        return currentUserVote;
    }

    public void setCurrentUserVote(String currentUserVote) {
        this.currentUserVote = currentUserVote;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }
}

