package com.blog.blog_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/media")
public class PublicMediaController {

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping("/{tenantId}/{filename}")
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
                    .header("Cache-Control", "public, max-age=31536000") // Cache for 1 year
                    .body(fileContent);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}