package com.finpulse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FeedbackDto {
    public Long id;
    public String authorName;
    public String authorRole;
    @NotNull @Min(1) @Max(5)
    public Integer rating;
    @NotBlank
    public String category;
    @NotBlank
    public String message;
    public String createdAt;
}
