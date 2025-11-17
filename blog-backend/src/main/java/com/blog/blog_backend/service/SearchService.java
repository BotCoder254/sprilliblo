package com.blog.blog_backend.service;

import com.blog.blog_backend.dto.SearchResultDto;
import com.blog.blog_backend.model.Post;
import com.blog.blog_backend.model.User;
import com.blog.blog_backend.repository.PostRepository;
import com.blog.blog_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Service
public class SearchService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public SearchResultDto search(String query, String tenantId, int limit) {
        if (query == null || query.trim().isEmpty() || tenantId == null) {
            return new SearchResultDto(List.of(), List.of(), List.of(), false);
        }

        // Limit search term length to prevent potential issues
        String searchTerm = query.trim();
        if (searchTerm.length() > 100) {
            searchTerm = searchTerm.substring(0, 100);
        }

        try {
            Pattern pattern = Pattern.compile(Pattern.quote(searchTerm), Pattern.CASE_INSENSITIVE);

            // Search posts
            List<SearchResultDto.PostSearchResult> posts = searchPosts(pattern, tenantId, Math.min(limit, 20));

            // Search tags
            List<SearchResultDto.TagSearchResult> tags = searchTags(pattern, tenantId, Math.min(limit, 20));

            // Search authors
            List<SearchResultDto.AuthorSearchResult> authors = searchAuthors(pattern, tenantId, Math.min(limit, 20));

            boolean hasMore = posts.size() >= limit || tags.size() >= limit || authors.size() >= limit;

            return new SearchResultDto(posts, tags, authors, hasMore);
        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
            return new SearchResultDto(List.of(), List.of(), List.of(), false);
        }
    }

    private List<SearchResultDto.PostSearchResult> searchPosts(Pattern pattern, String tenantId, int limit) {
        try {
            // Use simple repository query to avoid MongoDB converter issues
            List<Post> posts = postRepository.findByTenantIdAndStatusAndTitleContainingIgnoreCaseOrderByPublishedAtDesc(
                    tenantId, Post.PostStatus.PUBLISHED, pattern.pattern().replace("\\Q", "").replace("\\E", ""), 
                    PageRequest.of(0, limit)).getContent();

            return posts.stream()
                    .map(post -> new SearchResultDto.PostSearchResult(
                            post.getId() != null ? post.getId() : "",
                            post.getTitle() != null ? post.getTitle() : "",
                            post.getSlug() != null ? post.getSlug() : "",
                            post.getExcerpt() != null ? post.getExcerpt() : "",
                            post.getAuthor() != null ? post.getAuthor() : "",
                            post.getTags() != null ? post.getTags() : List.of()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error searching posts: " + e.getMessage());
            return List.of();
        }
    }

    private List<SearchResultDto.TagSearchResult> searchTags(Pattern pattern, String tenantId, int limit) {
        try {
            // Use PostService method instead of aggregation
            String searchTerm = pattern.pattern().replace("\\Q", "").replace("\\E", "");
            List<Post> posts = postRepository.findByTenantIdAndStatus(tenantId, Post.PostStatus.PUBLISHED);
            
            return posts.stream()
                    .filter(post -> post.getTags() != null)
                    .flatMap(post -> post.getTags().stream())
                    .filter(tag -> tag.toLowerCase().contains(searchTerm.toLowerCase()))
                    .distinct()
                    .limit(limit)
                    .map(tag -> new SearchResultDto.TagSearchResult(tag, 1L))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error searching tags: " + e.getMessage());
            return List.of();
        }
    }

    private List<SearchResultDto.AuthorSearchResult> searchAuthors(Pattern pattern, String tenantId, int limit) {
        try {
            String searchTerm = pattern.pattern().replace("\\Q", "").replace("\\E", "");
            List<Post> posts = postRepository.findByTenantIdAndStatus(tenantId, Post.PostStatus.PUBLISHED);
            
            return posts.stream()
                    .filter(post -> post.getAuthor() != null && 
                            post.getAuthor().toLowerCase().contains(searchTerm.toLowerCase()))
                    .map(post -> new SearchResultDto.AuthorSearchResult(
                            post.getAuthorId() != null ? post.getAuthorId() : "",
                            post.getAuthor(),
                            1L
                    ))
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error searching authors: " + e.getMessage());
            return List.of();
        }
    }
}