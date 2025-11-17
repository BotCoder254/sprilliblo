package com.blog.blog_backend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.blog.blog_backend.model.Post;

import java.util.List;
import java.util.ArrayList;

@Data
public class PostRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    private String slug;

    @Size(max = 500, message = "Excerpt must be less than 500 characters")
    private String excerpt;

    @Size(max = 5242880, message = "Content must be less than 5MB")
    private String bodyHtml;

    private String bodyMarkdown;
    private List<String> tags = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private String coverImageUrl;
    private Post.PostStatus status = Post.PostStatus.DRAFT;
}