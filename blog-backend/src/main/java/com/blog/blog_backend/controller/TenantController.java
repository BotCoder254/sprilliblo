package com.blog.blog_backend.controller;

import com.blog.blog_backend.dto.AuthResponse;
import com.blog.blog_backend.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/check-slug/{slug}")
    public ResponseEntity<Map<String, Object>> checkSlugAvailability(@PathVariable String slug) {
        try {
            boolean available = tenantService.isSlugAvailable(slug);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("available", available);
            if (!available) {
                response.put("suggestions", tenantService.generateSlugSuggestions(slug));
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<AuthResponse> createTenant(@RequestBody Map<String, String> request,
                                                     HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            AuthResponse response = tenantService.createTenantForUser(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}