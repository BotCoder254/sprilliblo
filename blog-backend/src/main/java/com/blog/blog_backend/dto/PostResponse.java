package com.blog.blog_backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import com.blog.blog_backend.model.Post;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PostResponse {
    private String id;
    private String title;
    private String slug;
    private String excerpt;
    private String bodyHtml;
    private String bodyMarkdown;
    private List<String> tags;
    private List<String> categories;
    private String coverImageUrl;
    private Post.PostStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long views;
    private AuthorDto author;
    
    @Data
    @AllArgsConstructor
    public static class AuthorDto {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
    }
}