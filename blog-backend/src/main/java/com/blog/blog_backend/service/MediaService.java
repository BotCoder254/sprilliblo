package com.blog.blog_backend.service;

import com.blog.blog_backend.model.Media;
import com.blog.blog_backend.repository.MediaRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class MediaService {
    
    private final MediaRepository mediaRepository;
    private final Cloudinary cloudinary;
    
    public MediaService(MediaRepository mediaRepository, @Value("${cloudinary.url}") String cloudinaryUrl) {
        this.mediaRepository = mediaRepository;
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }
    
    public Media uploadFile(String tenantId, String userId, MultipartFile file) throws IOException {
        validateFile(file);
        
        String publicId = generatePublicId(tenantId);
        
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
            ObjectUtils.asMap(
                "public_id", publicId,
                "folder", "sprilliblo/" + tenantId,
                "resource_type", "auto",
                "transformation", ObjectUtils.asMap(
                    "quality", "auto",
                    "fetch_format", "auto"
                )
            )
        );
        
        Media media = new Media();
        media.setTenantId(tenantId);
        media.setUploadedBy(userId);
        media.setOriginalFilename(file.getOriginalFilename());
        media.setFilename(generateFilename(file.getOriginalFilename()));
        media.setMimeType(file.getContentType());
        media.setSize(file.getSize());
        media.setCloudinaryPublicId(publicId);
        media.setUrl((String) uploadResult.get("secure_url"));
        
        // Generate thumbnail URL
        String thumbnailUrl = cloudinary.url()
            .transformation(new com.cloudinary.Transformation()
                .width(300)
                .height(300)
                .crop("fill")
                .quality("auto")
                .fetchFormat("auto"))
            .generate(publicId);
        media.setThumbnailUrl(thumbnailUrl);
        
        if (uploadResult.get("width") != null) {
            media.setWidth((Integer) uploadResult.get("width"));
        }
        if (uploadResult.get("height") != null) {
            media.setHeight((Integer) uploadResult.get("height"));
        }
        
        return mediaRepository.save(media);
    }
    
    public Page<Media> getMedia(String tenantId, String type, Pageable pageable) {
        if ("image".equals(type)) {
            return mediaRepository.findByTenantIdAndMimeTypeStartingWithOrderByCreatedAtDesc(tenantId, "image/", pageable);
        }
        return mediaRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
    }
    
    public Media getMediaById(String tenantId, String mediaId) {
        return mediaRepository.findByTenantIdAndId(tenantId, mediaId)
            .orElseThrow(() -> new RuntimeException("Media not found"));
    }
    
    public void deleteMedia(String tenantId, String mediaId, String userId) throws IOException {
        Media media = getMediaById(tenantId, mediaId);
        
        if (!media.getUploadedBy().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this media");
        }
        
        // Delete from Cloudinary
        cloudinary.uploader().destroy(media.getCloudinaryPublicId(), ObjectUtils.emptyMap());
        
        // Delete from database
        mediaRepository.delete(media);
    }
    
    public Media updateMedia(String tenantId, String mediaId, String userId, String newFilename) {
        Media media = getMediaById(tenantId, mediaId);
        
        if (!media.getUploadedBy().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this media");
        }
        
        media.setFilename(newFilename);
        media.setUpdatedAt(LocalDateTime.now());
        
        return mediaRepository.save(media);
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new RuntimeException("File size exceeds 10MB limit");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            throw new RuntimeException("Only image and video files are allowed");
        }
    }
    
    private String generatePublicId(String tenantId) {
        return tenantId + "/" + UUID.randomUUID().toString();
    }
    
    private String generateFilename(String originalFilename) {
        if (originalFilename == null) return "untitled";
        
        String name = originalFilename;
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            name = name.substring(0, lastDot);
        }
        
        return name.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}