package com.blog.blog_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class CommentRequest {
    @NotBlank(message = "Author name is required")
    @Size(max = 100, message = "Author name must be less than 100 characters")
    private String authorName;
    
    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String authorEmail;
    
    @NotBlank(message = "Comment body is required")
    @Size(max = 2000, message = "Comment must be less than 2000 characters")
    private String body;
    
    private String honeypot; // Spam protection

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getHoneypot() { return honeypot; }
    public void setHoneypot(String honeypot) { this.honeypot = honeypot; }
}