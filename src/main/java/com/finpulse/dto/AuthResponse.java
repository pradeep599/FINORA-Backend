package com.finpulse.dto;

public class AuthResponse {
    public String token;
    public Long userId;
    public String fullName;
    public String email;
    public String currency;

    public AuthResponse(String token, Long userId, String fullName, String email, String currency) {
        this.token = token; this.userId = userId; this.fullName = fullName;
        this.email = email; this.currency = currency;
    }
}
