package com.blog.blog_backend.controller;

import com.blog.blog_backend.model.Post;
import com.blog.blog_backend.model.Tenant;
import com.blog.blog_backend.service.PostService;
import com.blog.blog_backend.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/tenants/{tenantSlug}")
public class RSSController {

    @Autowired
    private PostService postService;

    @Autowired
    private TenantService tenantService;

    @GetMapping(value = "/rss.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getRSSFeed(@PathVariable String tenantSlug) {
        Optional<Tenant> tenant = tenantService.findBySlug(tenantSlug);
        if (tenant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Pageable pageable = PageRequest.of(0, 20, Sort.by("publishedAt").descending());
        List<Post> posts = postService.findPublishedPosts(tenant.get().getId(), null, null, null, pageable).getContent();

        String rssXml = generateRSSXML(tenant.get(), posts);

        return ResponseEntity.ok()
                .header("Content-Type", "application/rss+xml; charset=utf-8")
                .body(rssXml);
    }

    private String generateRSSXML(Tenant tenant, List<Post> posts) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\">\n");
        xml.append("  <channel>\n");
        xml.append("    <title>").append(escapeXml(tenant.getName())).append("</title>\n");
        xml.append("    <description>").append(escapeXml(tenant.getDescription() != null ? tenant.getDescription() : "")).append("</description>\n");
        xml.append("    <link>").append("https://").append(tenant.getSlug()).append(".sprilliblo.com").append("</link>\n");
        xml.append("    <language>en-us</language>\n");

        for (Post post : posts) {
            xml.append("    <item>\n");
            xml.append("      <title>").append(escapeXml(post.getTitle())).append("</title>\n");
            xml.append("      <description>").append(escapeXml(post.getExcerpt() != null ? post.getExcerpt() : "")).append("</description>\n");
            xml.append("      <link>").append("https://").append(tenant.getSlug()).append(".sprilliblo.com/posts/").append(post.getSlug()).append("</link>\n");
            xml.append("      <guid>").append("https://").append(tenant.getSlug()).append(".sprilliblo.com/posts/").append(post.getSlug()).append("</guid>\n");
            xml.append("      <pubDate>").append(post.getPublishedAt().format(DateTimeFormatter.RFC_1123_DATE_TIME)).append("</pubDate>\n");
            xml.append("      <author>").append(escapeXml(post.getAuthor())).append("</author>\n");
            
            if (post.getTags() != null && !post.getTags().isEmpty()) {
                for (String tag : post.getTags()) {
                    xml.append("      <category>").append(escapeXml(tag)).append("</category>\n");
                }
            }
            
            xml.append("    </item>\n");
        }

        xml.append("  </channel>\n");
        xml.append("</rss>");

        return xml.toString();
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}