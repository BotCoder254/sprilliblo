package com.blog.blog_backend.controller;

import com.blog.blog_backend.model.Tenant;
import com.blog.blog_backend.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tenants/{tenantId}/seo")
public class SeoController {

    private final TenantService tenantService;

    public SeoController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public ResponseEntity<Tenant.SeoSettings> getSeoSettings(@PathVariable String tenantId, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            Tenant.SeoSettings seoSettings = tenantService.getSeoSettings(tenantId, userId);
            return ResponseEntity.ok(seoSettings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping
    public ResponseEntity<Tenant.SeoSettings> updateSeoSettings(
            @PathVariable String tenantId,
            @RequestBody Tenant.SeoSettings seoSettings,
            HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            Tenant.SeoSettings updated = tenantService.updateSeoSettings(tenantId, userId, seoSettings);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewSeoSettings(
            @PathVariable String tenantId,
            @RequestBody Tenant.SeoSettings seoSettings,
            HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            Map<String, Object> preview = tenantService.generateSeoPreview(tenantId, seoSettings);
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}