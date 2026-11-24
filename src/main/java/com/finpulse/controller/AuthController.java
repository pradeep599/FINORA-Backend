package com.finpulse.controller;

import com.finpulse.dto.*;
import com.finpulse.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "Finora / FinPulse AI Backend");
    }

    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/auth/oauth")
    public ResponseEntity<AuthResponse> oauth(@Valid @RequestBody OAuthRequest req) {
        return ResponseEntity.ok(authService.oauthLogin(req));
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return ResponseEntity.ok(authService.forgotPassword(req));
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        return ResponseEntity.ok(authService.resetPassword(req));
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        return ResponseEntity.ok(authService.getProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<AuthResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest req) {
        return ResponseEntity.ok(authService.updateProfile(req));
    }
}
