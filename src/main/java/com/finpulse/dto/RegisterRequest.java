package com.finpulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    public String fullName;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    public String email;
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    public String password;
    public String currency = "INR";
}
