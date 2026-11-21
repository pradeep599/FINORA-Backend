package com.finpulse.dto;

import jakarta.validation.constraints.NotBlank;

public class ProfileUpdateRequest {
    @NotBlank(message = "Full name is required")
    public String fullName;
    @NotBlank(message = "Currency is required")
    public String currency;
    public String currentPassword;
    public String newPassword;
}
