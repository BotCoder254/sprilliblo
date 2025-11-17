package com.blog.blog_backend.controller;

import com.blog.blog_backend.dto.CommentRequest;
import com.blog.blog_backend.dto.CommentResponse;
import com.blog.blog_backend.model.Comment;
import com.blog.blog_backend.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenants/{tenantId}")
public class CommentController {
    
    private final CommentService commentService;
    
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable String tenantId,
            @PathVariable String postId,
            @Valid @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {
        
        String userId = (String) httpRequest.getAttribute("userId");
        CommentResponse comment = commentService.createComment(tenantId, postId, request, userId);
        return ResponseEntity.ok(comment);
    }
    
    @GetMapping("/comments")
    public ResponseEntity<Page<Comment>> getPendingComments(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.getPendingComments(tenantId, pageable);
        return ResponseEntity.ok(comments);
    }
    
    @PutMapping("/comments/{commentId}/approve")
    public ResponseEntity<Void> approveComment(
            @PathVariable String tenantId,
            @PathVariable String commentId,
            HttpServletRequest httpRequest) {
        
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        commentService.approveComment(tenantId, commentId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/comments/{commentId}/reject")
    public ResponseEntity<Void> rejectComment(
            @PathVariable String tenantId,
            @PathVariable String commentId,
            HttpServletRequest httpRequest) {
        
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        commentService.rejectComment(tenantId, commentId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String tenantId,
            @PathVariable String commentId,
            HttpServletRequest httpRequest) {
        
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        commentService.deleteComment(tenantId, commentId);
        return ResponseEntity.ok().build();
    }
}