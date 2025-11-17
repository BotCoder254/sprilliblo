package com.blog.blog_backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {
    @Id
    private String id;
    
    @Indexed
    private String tenantId;
    
    @Indexed
    private String postId;
    
    private String authorName;
    private String authorEmail;
    private String authorId; // For logged-in users
    private String body;
    
    @Indexed
    private CommentStatus status = CommentStatus.PENDING;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum CommentStatus {
        PENDING, APPROVED, REJECTED
    }
}