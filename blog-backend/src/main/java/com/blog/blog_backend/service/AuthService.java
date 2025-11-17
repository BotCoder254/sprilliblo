package com.blog.blog_backend.service;

import com.blog.blog_backend.dto.*;
import com.blog.blog_backend.model.User;
import com.blog.blog_backend.model.Tenant;
import com.blog.blog_backend.repository.UserRepository;
import com.blog.blog_backend.repository.TenantRepository;
import com.blog.blog_backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthService(UserRepository userRepository, TenantRepository tenantRepository,
                      PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        // Get user's tenants
        List<Tenant> userTenants = tenantRepository.findByMembersUserId(user.getId());
        
        // Set current tenant if not set
        String currentTenantId = user.getCurrentTenantId();
        if (currentTenantId == null && !userTenants.isEmpty()) {
            currentTenantId = userTenants.get(0).getId();
            user.setCurrentTenantId(currentTenantId);
            userRepository.save(user);
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), currentTenantId);
        
        return new AuthResponse(token, mapToUserDto(user, userTenants));
    }
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create user only
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user = userRepository.save(user);
        
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), null);
        
        return new AuthResponse(token, mapToUserDto(user, List.of()));
    }
    
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        User.ResetToken token = new User.ResetToken();
        token.setToken(resetToken);
        token.setExpiresAt(LocalDateTime.now().plusHours(1)); // 1 hour expiry
        
        user.setResetToken(token);
        userRepository.save(user);
        
        // TODO: Send email with reset link
        // emailService.sendPasswordResetEmail(email, resetToken);
    }
    
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetTokenToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));
        
        if (user.getResetToken().getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }
    
    public AuthResponse.UserDto getCurrentUser(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Tenant> userTenants = tenantRepository.findByMembersUserId(userId);
        return mapToUserDto(user, userTenants);
    }
    
    private AuthResponse.UserDto mapToUserDto(User user, List<Tenant> tenants) {
        List<AuthResponse.TenantDto> tenantDtos = tenants.stream()
            .map(tenant -> {
                String role = tenant.getMembers().stream()
                    .filter(member -> member.getUserId().equals(user.getId()))
                    .findFirst()
                    .map(Tenant.TenantMember::getRole)
                    .orElse("VIEWER");
                return new AuthResponse.TenantDto(tenant.getId(), tenant.getName(), tenant.getSlug(), role);
            })
            .collect(Collectors.toList());
        
        AuthResponse.TenantDto currentTenant = tenantDtos.stream()
            .filter(t -> t.getId().equals(user.getCurrentTenantId()))
            .findFirst()
            .orElse(tenantDtos.isEmpty() ? null : tenantDtos.get(0));
        
        return new AuthResponse.UserDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.isEmailVerified(),
            tenantDtos,
            currentTenant
        );
    }
}