package com.blog.blog_backend.dto;

import com.blog.blog_backend.model.Comment;
import java.time.LocalDateTime;

public class CommentResponse {
    private String id;
    private String authorName;
    private String body;
    private Comment.CommentStatus status;
    private LocalDateTime createdAt;

    public CommentResponse(String id, String authorName, String body, Comment.CommentStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.authorName = authorName;
        this.body = body;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getAuthorName() { return authorName; }
    public String getBody() { return body; }
    public Comment.CommentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}