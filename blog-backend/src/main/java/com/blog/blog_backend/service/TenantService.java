package com.blog.blog_backend.service;

import com.blog.blog_backend.dto.AuthResponse;
import com.blog.blog_backend.model.Tenant;
import com.blog.blog_backend.model.User;
import com.blog.blog_backend.repository.TenantRepository;
import com.blog.blog_backend.repository.UserRepository;
import com.blog.blog_backend.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    private static final Set<String> RESERVED_SLUGS = Set.of(
            "www", "admin", "api", "app", "blog", "help", "support", "about",
            "contact", "privacy", "terms", "login", "register", "dashboard",
            "settings", "profile", "account", "billing", "docs", "documentation"
    );

    public TenantService(TenantRepository tenantRepository, UserRepository userRepository,
                         JwtUtil jwtUtil, AuthService authService) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    public boolean isSlugAvailable(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return false;
        }

        String cleanSlug = slug.toLowerCase().trim();

        // Check if reserved
        if (RESERVED_SLUGS.contains(cleanSlug)) {
            return false;
        }

        // Check if already exists
        return !tenantRepository.existsBySlug(cleanSlug);
    }

    public List<String> generateSlugSuggestions(String originalSlug) {
        String baseSlug = originalSlug.toLowerCase().trim();

        return IntStream.range(1, 6)
                .mapToObj(i -> baseSlug + "-" + i)
                .filter(this::isSlugAvailable)
                .limit(3)
                .collect(Collectors.toList());
    }

    public boolean isValidSlugFormat(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return false;
        }

        String cleanSlug = slug.trim();

        // Check length
        if (cleanSlug.length() < 3 || cleanSlug.length() > 50) {
            return false;
        }

        // Check format: only lowercase letters, numbers, and hyphens
        return cleanSlug.matches("^[a-z0-9-]+$") &&
                !cleanSlug.startsWith("-") &&
                !cleanSlug.endsWith("-") &&
                !cleanSlug.contains("--");
    }

    public AuthResponse createTenantForUser(String userId, Map<String, String> request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String blogName = request.get("blogName");
        String blogSlug = request.get("blogSlug");

        if (blogName == null || blogName.trim().isEmpty()) {
            throw new RuntimeException("Blog name is required");
        }

        if (!isValidSlugFormat(blogSlug)) {
            throw new RuntimeException("Invalid blog slug format");
        }

        if (!isSlugAvailable(blogSlug)) {
            throw new RuntimeException("Blog slug is already taken");
        }

        // Create tenant
        Tenant tenant = new Tenant();
        tenant.setName(blogName.trim());
        tenant.setSlug(blogSlug.toLowerCase().trim());
        tenant.setOwnerId(userId);

        // Add user as owner
        Tenant.TenantMember owner = new Tenant.TenantMember();
        owner.setUserId(userId);
        owner.setRole("OWNER");
        tenant.getMembers().add(owner);

        tenant = tenantRepository.save(tenant);

        // Update user with tenant info
        user.getTenantIds().add(tenant.getId());
        user.setCurrentTenantId(tenant.getId());
        userRepository.save(user);

        // Generate new token with tenant context
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), tenant.getId());

        // Return updated user info
        AuthResponse.UserDto userDto = authService.getCurrentUser(userId);
        return new AuthResponse(token, userDto);
    }

    public java.util.Optional<Tenant> findBySlug(String slug) {
        return tenantRepository.findBySlug(slug);
    }

    public Tenant.SeoSettings getSeoSettings(String tenantId, String userId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        if (!canUserAccessTenant(userId, tenant)) {
            throw new RuntimeException("Access denied");
        }
        
        // Ensure SEO settings exist
        if (tenant.getSettings() == null) {
            tenant.setSettings(new Tenant.BlogSettings());
        }
        if (tenant.getSettings().getSeo() == null) {
            tenant.getSettings().setSeo(new Tenant.SeoSettings());
        }
        
        return tenant.getSettings().getSeo();
    }

    public Tenant.SeoSettings updateSeoSettings(String tenantId, String userId, Tenant.SeoSettings seoSettings) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        if (!canUserAccessTenant(userId, tenant)) {
            throw new RuntimeException("Access denied");
        }
        
        tenant.getSettings().setSeo(seoSettings);
        tenant.setUpdatedAt(java.time.LocalDateTime.now());
        tenantRepository.save(tenant);
        
        return seoSettings;
    }

    public Map<String, Object> generateSeoPreview(String tenantId, Tenant.SeoSettings seoSettings) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        Map<String, Object> preview = new java.util.HashMap<>();
        
        // Google Search Preview
        Map<String, String> googlePreview = new java.util.HashMap<>();
        googlePreview.put("title", seoSettings.getMetaTitle() != null ? seoSettings.getMetaTitle() : tenant.getName());
        googlePreview.put("description", seoSettings.getMetaDescription() != null ? seoSettings.getMetaDescription() : tenant.getDescription());
        googlePreview.put("url", "https://" + tenant.getSlug() + ".sprilliblo.com");
        preview.put("google", googlePreview);
        
        // Facebook Preview
        Map<String, String> facebookPreview = new java.util.HashMap<>();
        facebookPreview.put("title", seoSettings.getOgTitle() != null ? seoSettings.getOgTitle() : seoSettings.getMetaTitle());
        facebookPreview.put("description", seoSettings.getOgDescription() != null ? seoSettings.getOgDescription() : seoSettings.getMetaDescription());
        facebookPreview.put("image", seoSettings.getOgImage());
        facebookPreview.put("url", "https://" + tenant.getSlug() + ".sprilliblo.com");
        preview.put("facebook", facebookPreview);
        
        // Twitter Preview
        Map<String, String> twitterPreview = new java.util.HashMap<>();
        twitterPreview.put("title", seoSettings.getTwitterTitle() != null ? seoSettings.getTwitterTitle() : seoSettings.getOgTitle());
        twitterPreview.put("description", seoSettings.getTwitterDescription() != null ? seoSettings.getTwitterDescription() : seoSettings.getOgDescription());
        twitterPreview.put("image", seoSettings.getTwitterImage() != null ? seoSettings.getTwitterImage() : seoSettings.getOgImage());
        twitterPreview.put("card", seoSettings.getTwitterCard());
        preview.put("twitter", twitterPreview);
        
        return preview;
    }

    private boolean canUserAccessTenant(String userId, Tenant tenant) {
        return tenant.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(userId));
    }
}