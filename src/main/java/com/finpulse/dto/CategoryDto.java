package com.finpulse.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryDto {
    public Long id;
    @NotBlank(message = "Category name is required")
    public String name;
    @NotBlank(message = "Type is required")
    public String type;
    public String color;
    public String icon;
}
