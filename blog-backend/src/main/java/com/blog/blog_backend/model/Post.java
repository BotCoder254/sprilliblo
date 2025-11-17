package com.blog.blog_backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
@CompoundIndex(def = "{'tenantId': 1, 'slug': 1}", unique = true)
public class Post {
    @Id
    private String id;

    @Indexed
    private String tenantId;

    @Indexed
    private String authorId;

    private String title;
    private String slug;
    private String excerpt;

    @org.springframework.data.mongodb.core.mapping.Field
    private String bodyHtml;

    private String bodyMarkdown;
    private String content; // For public API
    private String author; // Author name for public API
    private String featuredImage; // For public API
    private List<String> tags = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private String coverImageUrl;

    @Indexed
    private PostStatus status = PostStatus.DRAFT;

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private Long views = 0L;

    public enum PostStatus {
        DRAFT, PUBLISHED
    }
}