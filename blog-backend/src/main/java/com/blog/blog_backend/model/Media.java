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
@Document(collection = "media")
public class Media {
    @Id
    private String id;
    
    @Indexed
    private String tenantId;
    
    private String url;
    private String thumbnailUrl;
    private String filename;
    private String originalFilename;
    private String mimeType;
    private Long size;
    private Integer width;
    private Integer height;
    
    @Indexed
    private String uploadedBy;
    
    private String cloudinaryPublicId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}