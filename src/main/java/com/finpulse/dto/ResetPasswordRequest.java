package com.finpulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotBlank @Email
    public String email;
    @NotBlank(message = "Reset code is required")
    public String token;
    @NotBlank @Size(min = 6, message = "New password must be at least 6 characters")
    public String newPassword;
}
