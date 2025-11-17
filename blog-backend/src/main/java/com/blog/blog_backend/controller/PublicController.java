package com.blog.blog_backend.controller;

import com.blog.blog_backend.dto.PostResponse;
import com.blog.blog_backend.model.Post;
import com.blog.blog_backend.model.Tenant;
import com.blog.blog_backend.service.PostService;
import com.blog.blog_backend.service.TenantService;
import com.blog.blog_backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/tenants")
public class PublicController {

    @Autowired
    private PostService postService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private CommentService commentService;


    @GetMapping("/{tenantSlug}")
    public ResponseEntity<Tenant> getTenant(@PathVariable String tenantSlug) {
        Optional<Tenant> tenant = tenantService.findBySlug(tenantSlug);
        if (tenant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(30)))
                .body(tenant.get());
    }

    @GetMapping("/{tenantSlug}/posts")
    public ResponseEntity<Page<PostResponse>> getPublicPosts(
            @PathVariable String tenantSlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "publishedAt") String sort) {

        Optional<Tenant> tenant = tenantService.findBySlug(tenantSlug);
        if (tenant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Sort.Direction direction = Sort.Direction.DESC;
        String sortField = sort.equals("views") ? "views" : "publishedAt";
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Post> posts = postService.findPublishedPosts(tenant.get().getId(), tag, author, q, pageable);

        Page<PostResponse> postDTOs = posts.map(this::convertToPublicDTO);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
                .body(postDTOs);
    }

    @GetMapping("/{tenantSlug}/posts/{slug}")
    public ResponseEntity<PostResponse> getPublicPost(
            @PathVariable String tenantSlug,
            @PathVariable String slug) {

        Optional<Tenant> tenant = tenantService.findBySlug(tenantSlug);
        if (tenant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Post> post = postService.findPublishedBySlug(tenant.get().getId(), slug);
        if (post.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PostResponse postDTO = convertToPublicDTO(post.get());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                .body(postDTO);
    }

    @GetMapping("/{tenantSlug}/posts/{slug}/related")
    public ResponseEntity<List<PostResponse>> getRelatedPosts(
            @PathVariable String tenantSlug,
            @PathVariable String slug) {

        Optional<Tenant> tenant = tenantService.findBySlug(tenantSlug);
        if (tenant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Post> relatedPosts = postService.findRelatedPosts(tenant.get().getId(), slug, 3);
        List<PostResponse> postDTOs = relatedPosts.stream().map(this::convertToPublicDTO).toList();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(15)))
                .body(postDTOs);
    }


    @GetMapping("/{tenantSlug}/tags")
    public ResponseEntity<List<String>> getTags(
            @PathVariable String tenantSlug,
            @RequestParam(required = false) String query) {

        Optional<Tenant> tenant = tenantService.findBySlug(tenantSlug);
        if (tenant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<String> tags = postService.getPopularTags(tenant.get().getId(), query, 20);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                .body(tags);
    }

    private PostResponse convertToPublicDTO(Post post) {
        // Get approved comments count for this post
        long commentsCount = commentService.getApprovedComments(post.getTenantId(), post.getId()).size();
        
        PostResponse response = new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getContent() != null ? post.getContent() : post.getBodyHtml(),
                post.getBodyMarkdown(),
                post.getTags(),
                post.getCategories(),
                post.getFeaturedImage() != null ? post.getFeaturedImage() : post.getCoverImageUrl(),
                post.getStatus(),
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getViews(),
                new PostResponse.AuthorDto(post.getAuthorId(), post.getAuthor() != null ? post.getAuthor() : "Anonymous", "", ""),
                calculateReadTime(post.getContent() != null ? post.getContent() : post.getBodyHtml()),
                commentsCount
        );
        
        return response;
    }

    private int calculateReadTime(String content) {
        if (content == null || content.isEmpty()) return 1;

        // Remove HTML tags and count words
        String plainText = content.replaceAll("<[^>]*>", "");
        String[] words = plainText.trim().split("\\s+");
        int wordCount = words.length;

        // Average reading speed: 200 words per minute
        int readTime = Math.max(1, (int) Math.ceil(wordCount / 200.0));
        return readTime;
    }
}