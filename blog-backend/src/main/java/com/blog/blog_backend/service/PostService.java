package com.blog.blog_backend.service;

import com.blog.blog_backend.dto.PostRequest;
import com.blog.blog_backend.dto.PostResponse;
import com.blog.blog_backend.model.Post;
import com.blog.blog_backend.model.User;
import com.blog.blog_backend.repository.PostRepository;
import com.blog.blog_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private NotificationService notificationService;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public PostResponse createPost(String tenantId, String authorId, PostRequest request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Post post = new Post();
        post.setTenantId(tenantId);
        post.setAuthorId(authorId);
        post.setTitle(request.getTitle());
        post.setSlug(generateUniqueSlug(tenantId, request.getSlug() != null ? request.getSlug() : request.getTitle()));
        post.setExcerpt(request.getExcerpt());
        post.setBodyHtml(sanitizeHtml(request.getBodyHtml()));
        post.setContent(sanitizeHtml(request.getBodyHtml()));
        post.setAuthor(author.getFirstName() + " " + author.getLastName());
        post.setFeaturedImage(request.getCoverImageUrl());
        post.setBodyMarkdown(request.getBodyMarkdown());
        post.setTags(request.getTags());
        post.setCategories(request.getCategories());
        post.setCoverImageUrl(request.getCoverImageUrl());
        post.setStatus(request.getStatus());

        if (request.getStatus() == Post.PostStatus.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());

            // Create notification for post publication
            try {
                notificationService.createPostPublishedNotification(
                        tenantId,
                        authorId,
                        post.getTitle(),
                        post.getSlug()
                );
            } catch (Exception e) {
                System.err.println("Failed to create post published notification: " + e.getMessage());
            }
        }

        post = postRepository.save(post);
        return mapToResponse(post, author);
    }

    public PostResponse updatePost(String tenantId, String postId, String userId, PostRequest request) {
        Post post = postRepository.findByTenantIdAndId(tenantId, postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User author = userRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));

        // Check if user can edit this post
        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("You can only edit your own posts");
        }

        post.setTitle(request.getTitle());

        // Update slug if changed and ensure uniqueness
        if (request.getSlug() != null && !request.getSlug().equals(post.getSlug())) {
            post.setSlug(generateUniqueSlug(tenantId, request.getSlug(), post.getId()));
        }

        post.setExcerpt(request.getExcerpt());
        post.setBodyHtml(sanitizeHtml(request.getBodyHtml()));
        post.setContent(sanitizeHtml(request.getBodyHtml()));
        post.setAuthor(author.getFirstName() + " " + author.getLastName());
        post.setFeaturedImage(request.getCoverImageUrl());
        post.setBodyMarkdown(request.getBodyMarkdown());
        post.setTags(request.getTags());
        post.setCategories(request.getCategories());
        post.setCoverImageUrl(request.getCoverImageUrl());

        // Handle status change
        if (request.getStatus() == Post.PostStatus.PUBLISHED && post.getStatus() == Post.PostStatus.DRAFT) {
            post.setPublishedAt(LocalDateTime.now());

            // Create notification for post publication
            try {
                notificationService.createPostPublishedNotification(
                        tenantId,
                        userId,
                        post.getTitle(),
                        post.getSlug()
                );
            } catch (Exception e) {
                System.err.println("Failed to create post published notification: " + e.getMessage());
            }
        }
        post.setStatus(request.getStatus());
        post.setUpdatedAt(LocalDateTime.now());

        post = postRepository.save(post);
        return mapToResponse(post, author);
    }

    public Page<PostResponse> getPosts(String tenantId, int page, int size, Post.PostStatus status, String tag, String category, String author) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = getFilteredPosts(tenantId, status, tag, category, author, pageable);

        return posts.map(post -> {
            User postAuthor = userRepository.findById(post.getAuthorId()).orElse(null);
            return mapToResponse(post, postAuthor);
        });
    }

    public Page<PostResponse> getPosts(String tenantId, int page, int size, Post.PostStatus status) {
        return getPosts(tenantId, page, size, status, null, null, null);
    }

    private Page<Post> getFilteredPosts(String tenantId, Post.PostStatus status, String tag, String category, String author, Pageable pageable) {
        Query query = new Query();
        Criteria criteria = Criteria.where("tenantId").is(tenantId);

        if (status != null) {
            criteria = criteria.and("status").is(status);
        }

        if (tag != null && !tag.trim().isEmpty()) {
            criteria = criteria.and("tags").in(tag.trim());
        }

        if (category != null && !category.trim().isEmpty()) {
            criteria = criteria.and("categories").in(category.trim());
        }

        if (author != null && !author.trim().isEmpty()) {
            criteria = criteria.and("author").regex(author.trim(), "i");
        }

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "updatedAt"));
        query.with(pageable);

        List<Post> posts = mongoTemplate.find(query, Post.class);
        long total = mongoTemplate.count(query.skip(0).limit(0), Post.class);

        return new PageImpl<>(posts, pageable, total);
    }

    public PostResponse getPost(String tenantId, String postId) {
        Post post = postRepository.findByTenantIdAndId(tenantId, postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User author = userRepository.findById(post.getAuthorId()).orElse(null);
        return mapToResponse(post, author);
    }

    public PostResponse getPostBySlug(String tenantId, String slug) {
        Post post = postRepository.findByTenantIdAndSlug(tenantId, slug)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Increment views
        post.setViews((post.getViews() != null ? post.getViews() : 0L) + 1L);
        post = postRepository.save(post);
        
        // Send real-time view update via WebSocket
        try {
            notificationService.sendViewUpdate(tenantId, post.getId(), post.getViews().intValue());
        } catch (Exception e) {
            System.err.println("Failed to send view update: " + e.getMessage());
        }

        User author = userRepository.findById(post.getAuthorId()).orElse(null);
        return mapToResponse(post, author);
    }

    public void deletePost(String tenantId, String postId, String userId) {
        Post post = postRepository.findByTenantIdAndId(tenantId, postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("You can only delete your own posts");
        }

        postRepository.delete(post);
    }

    public List<String> generateSlugSuggestions(String tenantId, String baseSlug) {
        return IntStream.range(1, 6)
                .mapToObj(i -> baseSlug + "-" + i)
                .filter(slug -> !postRepository.existsByTenantIdAndSlug(tenantId, slug))
                .limit(3)
                .toList();
    }

    private String generateUniqueSlug(String tenantId, String title) {
        return generateUniqueSlug(tenantId, title, null);
    }

    private String generateUniqueSlug(String tenantId, String title, String excludePostId) {
        String baseSlug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "")
                .substring(0, Math.min(title.length(), 50));

        if (baseSlug.isEmpty()) {
            baseSlug = "post-" + UUID.randomUUID().toString().substring(0, 8);
        }

        String slug = baseSlug;
        int counter = 1;

        while (postRepository.existsByTenantIdAndSlug(tenantId, slug)) {
            // If this is the same post being updated, allow the slug
            if (excludePostId != null) {
                Post existingPost = postRepository.findByTenantIdAndSlug(tenantId, slug).orElse(null);
                if (existingPost != null && existingPost.getId().equals(excludePostId)) {
                    break;
                }
            }
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }

    private String sanitizeHtml(String html) {
        if (html == null) return null;

        // Enhanced HTML sanitization
        return html
                // Remove script tags and content
                .replaceAll("(?i)<script[^>]*>.*?</script>", "")
                // Remove javascript: protocols
                .replaceAll("(?i)javascript:", "")
                // Remove event handlers
                .replaceAll("(?i)\\s*on\\w+\\s*=\\s*['\"][^'\"]*['\"]?", "")
                // Remove style attributes (keep only safe inline styles if needed)
                .replaceAll("(?i)\\s*style\\s*=\\s*['\"][^'\"]*['\"]?", "")
                // Remove dangerous tags
                .replaceAll("(?i)</?(?:object|embed|applet|iframe|frame|frameset|meta|link|base)[^>]*>", "")
                // Clean up extra whitespace
                .replaceAll("\\s+", " ")
                .trim();
    }

    public Page<Post> findPublishedPosts(String tenantId, String tag, String author, String query, Pageable pageable) {
        if (query != null && !query.trim().isEmpty()) {
            return postRepository.findByTenantIdAndStatusAndTitleContainingIgnoreCaseOrderByPublishedAtDesc(
                    tenantId, Post.PostStatus.PUBLISHED, query.trim(), pageable);
        }
        if (tag != null && !tag.trim().isEmpty()) {
            return postRepository.findByTenantIdAndStatusAndTagsContainingOrderByPublishedAtDesc(
                    tenantId, Post.PostStatus.PUBLISHED, tag.trim(), pageable);
        }
        if (author != null && !author.trim().isEmpty()) {
            return postRepository.findByTenantIdAndStatusAndAuthorContainingIgnoreCaseOrderByPublishedAtDesc(
                    tenantId, Post.PostStatus.PUBLISHED, author.trim(), pageable);
        }
        return postRepository.findByTenantIdAndStatusOrderByPublishedAtDesc(tenantId, Post.PostStatus.PUBLISHED, pageable);
    }

    public java.util.Optional<Post> findPublishedBySlug(String tenantId, String slug) {
        return postRepository.findByTenantIdAndSlugAndStatus(tenantId, slug, Post.PostStatus.PUBLISHED);
    }

    public List<Post> findRelatedPosts(String tenantId, String currentSlug, int limit) {
        java.util.Optional<Post> currentPost = postRepository.findByTenantIdAndSlug(tenantId, currentSlug);
        if (currentPost.isEmpty() || currentPost.get().getTags() == null || currentPost.get().getTags().isEmpty()) {
            return postRepository.findByTenantIdAndStatusAndSlugNotOrderByPublishedAtDesc(
                    tenantId, Post.PostStatus.PUBLISHED, currentSlug, PageRequest.of(0, limit)).getContent();
        }

        List<String> tags = currentPost.get().getTags();
        return postRepository.findByTenantIdAndStatusAndTagsInAndSlugNotOrderByPublishedAtDesc(
                tenantId, Post.PostStatus.PUBLISHED, tags, currentSlug, PageRequest.of(0, limit)).getContent();
    }

    public List<String> getPopularTags(String tenantId, String query, int limit) {
        List<Post> posts = postRepository.findByTenantIdAndStatus(tenantId, Post.PostStatus.PUBLISHED);

        java.util.Map<String, Integer> tagCounts = new java.util.HashMap<>();
        for (Post post : posts) {
            if (post.getTags() != null) {
                for (String tag : post.getTags()) {
                    String normalizedTag = normalizeTag(tag);
                    if (query == null || normalizedTag.toLowerCase().contains(query.toLowerCase())) {
                        tagCounts.put(normalizedTag, tagCounts.getOrDefault(normalizedTag, 0) + 1);
                    }
                }
            }
        }

        return tagCounts.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(java.util.Map.Entry::getKey)
                .toList();
    }

    public String normalizeTag(String tag) {
        if (tag == null) return "";
        return tag.toLowerCase().trim().replaceAll("\\s+", "-");
    }

    public List<Post> findByTenantId(String tenantId) {
        return postRepository.findByTenantId(tenantId);
    }

    public List<Post> findRecentPublished(String tenantId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"));
        return postRepository.findByTenantIdAndStatusOrderByPublishedAtDesc(tenantId, Post.PostStatus.PUBLISHED, pageable).getContent();
    }

    private PostResponse mapToResponse(Post post, User author) {
        PostResponse.AuthorDto authorDto = null;
        if (author != null) {
            authorDto = new PostResponse.AuthorDto(
                    author.getId(),
                    author.getFirstName(),
                    author.getLastName(),
                    author.getEmail()
            );
        }

        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getBodyHtml(),
                post.getBodyMarkdown(),
                post.getTags(),
                post.getCategories(),
                post.getCoverImageUrl(),
                post.getStatus(),
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getViews() != null ? post.getViews() : 0L,
                authorDto,
                1,
                0L
        );
    }
}