package com.finpulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class OAuthRequest {
    @NotBlank(message = "Provider is required")
    public String provider; // GOOGLE or APPLE
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    public String email;
    public String fullName;
    public String idToken; // Optional Google ID token
}
