package com.blog.blog_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/public/help")
public class HelpController {

    @GetMapping("/seo")
    public ResponseEntity<Map<String, Object>> getSeoHelp() {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> categories = Arrays.asList(
            createCategory("seo-basics", "SEO Fundamentals", "Search", "text-blue-600 bg-blue-50 dark:bg-blue-900/20",
                Arrays.asList(
                    createArticle("Meta Title Optimization", "Write compelling titles under 60 characters", "meta-title"),
                    createArticle("Meta Description Best Practices", "Create engaging descriptions that drive clicks", "meta-description"),
                    createArticle("Keyword Research & Strategy", "Find and target the right keywords", "keywords"),
                    createArticle("Search Engine Indexing", "Control how search engines crawl your site", "indexing")
                )
            ),
            createCategory("social-media", "Social Media Optimization", "Globe", "text-green-600 bg-green-50 dark:bg-green-900/20",
                Arrays.asList(
                    createArticle("Open Graph Setup", "Optimize Facebook and LinkedIn sharing", "open-graph"),
                    createArticle("Twitter Cards Configuration", "Create rich Twitter previews", "twitter-cards"),
                    createArticle("Social Media Images", "Optimal image sizes and formats", "social-images"),
                    createArticle("Social Sharing Best Practices", "Maximize engagement and reach", "social-best-practices")
                )
            ),
            createCategory("technical-seo", "Technical SEO", "Settings", "text-purple-600 bg-purple-50 dark:bg-purple-900/20",
                Arrays.asList(
                    createArticle("Structured Data & Schema", "Rich snippets and search features", "structured-data"),
                    createArticle("Canonical URLs", "Prevent duplicate content issues", "canonical-urls"),
                    createArticle("XML Sitemaps", "Help search engines discover content", "sitemaps"),
                    createArticle("Page Speed Optimization", "Improve loading times for better rankings", "page-speed")
                )
            )
        );
        
        response.put("categories", categories);
        response.put("totalArticles", categories.stream().mapToInt(cat -> 
            ((List<?>) cat.get("articles")).size()).sum());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/articles/{slug}")
    public ResponseEntity<Map<String, Object>> getArticle(@PathVariable String slug) {
        Map<String, Object> article = getArticleContent(slug);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(article);
    }

    private Map<String, Object> createCategory(String id, String title, String icon, String color, List<Map<String, Object>> articles) {
        Map<String, Object> category = new HashMap<>();
        category.put("id", id);
        category.put("title", title);
        category.put("icon", icon);
        category.put("color", color);
        category.put("articles", articles);
        return category;
    }

    private Map<String, Object> createArticle(String title, String description, String slug) {
        Map<String, Object> article = new HashMap<>();
        article.put("title", title);
        article.put("description", description);
        article.put("slug", slug);
        return article;
    }

    private Map<String, Object> getArticleContent(String slug) {
        Map<String, String> articles = Map.of(
            "meta-title", "# Meta Title Optimization\n\nYour meta title is the first thing users see in search results. Keep it under 60 characters and include your primary keyword near the beginning.\n\n## Best Practices:\n- Include your main keyword\n- Keep it under 60 characters\n- Make it compelling and clickable\n- Avoid keyword stuffing",
            "meta-description", "# Meta Description Best Practices\n\nMeta descriptions appear below your title in search results. They should be 150-160 characters and provide a compelling summary of your content.\n\n## Tips:\n- Write a compelling summary\n- Include a call-to-action\n- Use your target keywords naturally\n- Keep it between 150-160 characters",
            "open-graph", "# Open Graph Setup\n\nOpen Graph tags control how your content appears when shared on Facebook, LinkedIn, and other social platforms.\n\n## Required Tags:\n- og:title\n- og:description\n- og:image (1200x630px recommended)\n- og:url\n- og:type",
            "twitter-cards", "# Twitter Cards Configuration\n\nTwitter Cards provide rich previews when your content is shared on Twitter.\n\n## Card Types:\n- Summary Card: Basic preview with small image\n- Summary Large Image: Featured image preview\n- App Card: Mobile app promotion\n- Player Card: Video/audio content"
        );
        
        String content = articles.get(slug);
        if (content == null) {
            return null;
        }
        
        Map<String, Object> article = new HashMap<>();
        article.put("slug", slug);
        article.put("content", content);
        article.put("lastUpdated", new Date());
        return article;
    }
}