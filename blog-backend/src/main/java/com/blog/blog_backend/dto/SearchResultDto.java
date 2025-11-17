package com.blog.blog_backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    private List<PostSearchResult> posts;
    private List<TagSearchResult> tags;
    private List<AuthorSearchResult> authors;
    private boolean hasMore;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostSearchResult {
        private String id;
        private String title;
        private String slug;
        private String excerpt;
        private String author;
        private List<String> tags;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagSearchResult {
        private String name;
        private long postCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorSearchResult {
        private String id;
        private String name;
        private long postCount;
    }
}