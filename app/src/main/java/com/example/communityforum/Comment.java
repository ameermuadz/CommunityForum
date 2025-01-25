package com.example.communityforum;

import java.util.Date;

public class Comment {
    private String commentId;
    private String content;
    private String posterId;
    private Date timestamp;
    public Comment() {}

    public Comment(String commentId, String content, String posterId, Date timestamp) {
        this.commentId = commentId;
        this.content = content;
        this.posterId = posterId;
        this.timestamp = timestamp;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
