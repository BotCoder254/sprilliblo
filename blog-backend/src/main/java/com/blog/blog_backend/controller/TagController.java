package com.blog.blog_backend.controller;

import com.blog.blog_backend.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/tags")
public class TagController {

    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<List<String>> getTags(
            @PathVariable String tenantId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request) {

        System.out.println("Getting tags for tenant: " + tenantId + ", query: " + query + ", limit: " + limit);
        
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            System.out.println("No userId found in request attributes");
            return ResponseEntity.status(401).build();
        }

        System.out.println("User ID: " + userId);
        List<String> tags = postService.getPopularTags(tenantId, query, limit);
        System.out.println("Found " + tags.size() + " tags: " + tags);
        return ResponseEntity.ok(tags);
    }
}