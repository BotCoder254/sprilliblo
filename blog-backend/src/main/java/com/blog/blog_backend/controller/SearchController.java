package com.blog.blog_backend.controller;

import com.blog.blog_backend.dto.SearchResultDto;
import com.blog.blog_backend.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResultDto> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {
        
        String tenantId = (String) authentication.getPrincipal();
        SearchResultDto results = searchService.search(q, tenantId, limit);
        return ResponseEntity.ok(results);
    }
}