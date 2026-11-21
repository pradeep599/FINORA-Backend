package com.finpulse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(nullable = false, length = 10)
    private String currency = "INR";
    @Column(nullable = false, length = 30)
    private String role = "USER";
    @Column(name = "auth_provider", nullable = false, length = 20)
    private String authProvider = "LOCAL";
    @Column(name = "reset_token", length = 100)
    private String resetToken;
    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}
    public User(String fullName, String email, String passwordHash, String currency) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.currency = currency != null ? currency : "INR";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
