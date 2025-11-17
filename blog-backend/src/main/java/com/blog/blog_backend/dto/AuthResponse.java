package com.blog.blog_backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import com.blog.blog_backend.model.User;
import com.blog.blog_backend.model.Tenant;

import java.util.List;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDto user;
    
    @Data
    @AllArgsConstructor
    public static class UserDto {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private boolean emailVerified;
        private List<TenantDto> tenants;
        private TenantDto currentTenant;
    }
    
    @Data
    @AllArgsConstructor
    public static class TenantDto {
        private String id;
        private String name;
        private String slug;
        private String role;
    }
}