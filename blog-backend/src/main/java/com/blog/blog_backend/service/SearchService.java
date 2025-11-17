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
        if (query == null || query.trim().isEmpty()) {
            return new SearchResultDto(List.of(), List.of(), List.of(), false);
        }

        String searchTerm = query.trim();
        Pattern pattern = Pattern.compile(searchTerm, Pattern.CASE_INSENSITIVE);

        // Search posts
        List<SearchResultDto.PostSearchResult> posts = searchPosts(pattern, tenantId, limit);

        // Search tags
        List<SearchResultDto.TagSearchResult> tags = searchTags(pattern, tenantId, limit);

        // Search authors
        List<SearchResultDto.AuthorSearchResult> authors = searchAuthors(pattern, tenantId, limit);

        boolean hasMore = posts.size() >= limit || tags.size() >= limit || authors.size() >= limit;

        return new SearchResultDto(posts, tags, authors, hasMore);
    }

    private List<SearchResultDto.PostSearchResult> searchPosts(Pattern pattern, String tenantId, int limit) {
        Query query = new Query();

        // Use text search if available, otherwise fall back to regex
        try {
            query.addCriteria(Criteria.where("tenantId").is(tenantId)
                    .and("status").is(Post.PostStatus.PUBLISHED)
                    .andOperator(Criteria.where("$text").is(Criteria.where("$search").is(pattern.pattern()))));
        } catch (Exception e) {
            // Fallback to regex search
            query.addCriteria(Criteria.where("tenantId").is(tenantId)
                    .and("status").is(Post.PostStatus.PUBLISHED)
                    .orOperator(
                            Criteria.where("title").regex(pattern),
                            Criteria.where("excerpt").regex(pattern),
                            Criteria.where("tags").regex(pattern)
                    ));
        }

        query.limit(limit);

        List<Post> posts = mongoTemplate.find(query, Post.class);

        return posts.stream()
                .map(post -> new SearchResultDto.PostSearchResult(
                        post.getId(),
                        post.getTitle(),
                        post.getSlug(),
                        post.getExcerpt(),
                        post.getAuthor(),
                        post.getTags()
                ))
                .collect(Collectors.toList());
    }

    private List<SearchResultDto.TagSearchResult> searchTags(Pattern pattern, String tenantId, int limit) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("tenantId").is(tenantId)
                        .and("status").is(Post.PostStatus.PUBLISHED)),
                Aggregation.unwind("tags"),
                Aggregation.match(Criteria.where("tags").regex(pattern)),
                Aggregation.group("tags").count().as("postCount"),
                Aggregation.project("postCount").and("_id").as("name"),
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "postCount"),
                Aggregation.limit(limit)
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "posts", Map.class);

        return results.getMappedResults().stream()
                .map(result -> new SearchResultDto.TagSearchResult(
                        (String) result.get("name"),
                        ((Number) result.get("postCount")).longValue()
                ))
                .collect(Collectors.toList());
    }

    private List<SearchResultDto.AuthorSearchResult> searchAuthors(Pattern pattern, String tenantId, int limit) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("tenantId").is(tenantId)
                        .and("status").is(Post.PostStatus.PUBLISHED)
                        .and("author").regex(pattern)),
                Aggregation.group("authorId", "author").count().as("postCount"),
                Aggregation.project("postCount").and("_id.author").as("name").and("_id.authorId").as("id"),
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "postCount"),
                Aggregation.limit(limit)
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "posts", Map.class);

        return results.getMappedResults().stream()
                .map(result -> {
                    SearchResultDto.AuthorSearchResult authorResult = new SearchResultDto.AuthorSearchResult();
                    authorResult.setId((String) result.get("id"));
                    authorResult.setName((String) result.get("name"));
                    authorResult.setPostCount(((Number) result.get("postCount")).longValue());
                    return authorResult;
                })
                .collect(Collectors.toList());
    }
}