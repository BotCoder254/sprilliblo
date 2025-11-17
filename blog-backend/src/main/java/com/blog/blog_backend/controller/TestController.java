package com.blog.blog_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        return ResponseEntity.ok(Map.of("message", "Backend is working!"));
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicTest() {
        return ResponseEntity.ok(Map.of("message", "Public endpoint is working!"));
    }
}