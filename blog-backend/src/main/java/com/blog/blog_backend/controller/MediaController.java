package com.blog.blog_backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants/{tenantId}/media-old")
public class MediaController {
    
    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @PathVariable String tenantId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {
        
        try {
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }
            
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }
            
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds 5MB limit"));
            }
            
            String contentType = file.getContentType();
            boolean isValidType = false;
            for (String allowedType : ALLOWED_TYPES) {
                if (allowedType.equals(contentType)) {
                    isValidType = true;
                    break;
                }
            }
            
            if (!isValidType) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file type. Only images are allowed"));
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            
            String filename = UUID.randomUUID().toString() + extension;
            String relativePath = tenantId + "/" + filename;
            
            // Create directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR + tenantId);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return URL
            String fileUrl = "/api/media/" + relativePath;
            
            return ResponseEntity.ok(Map.of(
                "url", fileUrl,
                "filename", filename,
                "size", String.valueOf(file.getSize())
            ));
            
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getFile(
            @PathVariable String tenantId,
            @PathVariable String filename) {
        
        try {
            Path filePath = Paths.get(UPLOAD_DIR + tenantId + "/" + filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            
            return ResponseEntity.ok()
                .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                .body(fileContent);
                
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}