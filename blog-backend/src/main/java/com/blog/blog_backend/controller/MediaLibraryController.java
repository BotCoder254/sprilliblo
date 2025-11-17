package com.blog.blog_backend.controller;

import com.blog.blog_backend.model.Media;
import com.blog.blog_backend.service.MediaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/tenants/{tenantId}/media")
public class MediaLibraryController {
    
    private final MediaService mediaService;
    
    public MediaLibraryController(MediaService mediaService) {
        this.mediaService = mediaService;
    }
    
    @PostMapping("/upload")
    public ResponseEntity<Media> uploadMedia(
            @PathVariable String tenantId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            Media media = mediaService.uploadFile(tenantId, userId, file);
            return ResponseEntity.ok(media);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<Page<Media>> getMedia(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Media> media = mediaService.getMedia(tenantId, type, pageable);
        
        return ResponseEntity.ok(media);
    }
    
    @GetMapping("/{mediaId}")
    public ResponseEntity<Media> getMediaById(
            @PathVariable String tenantId,
            @PathVariable String mediaId,
            HttpServletRequest request) {
        
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            Media media = mediaService.getMediaById(tenantId, mediaId);
            return ResponseEntity.ok(media);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{mediaId}")
    public ResponseEntity<Media> updateMedia(
            @PathVariable String tenantId,
            @PathVariable String mediaId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            String newFilename = request.get("filename");
            Media media = mediaService.updateMedia(tenantId, mediaId, userId, newFilename);
            return ResponseEntity.ok(media);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable String tenantId,
            @PathVariable String mediaId,
            HttpServletRequest request) {
        
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            mediaService.deleteMedia(tenantId, mediaId, userId);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}