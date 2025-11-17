package com.blog.blog_backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tenants")
public class Tenant {
    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;

    private String name;
    private String description;
    private String ownerId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Blog settings
    private BlogSettings settings = new BlogSettings();

    // Members with roles
    private List<TenantMember> members = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlogSettings {
        private String theme = "default";
        private boolean allowComments = true;
        private boolean isPublic = true;
        private String customDomain;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantMember {
        private String userId;
        private String role; // OWNER, ADMIN, EDITOR, VIEWER
        private LocalDateTime joinedAt = LocalDateTime.now();
    }
}