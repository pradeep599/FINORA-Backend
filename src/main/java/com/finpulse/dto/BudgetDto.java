package com.finpulse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BudgetDto {
    public Long id;
    @NotNull(message = "Category is required")
    public Long categoryId;
    public String categoryName;
    public String categoryColor;
    @NotNull(message = "Monthly limit is required")
    @DecimalMin(value = "1.00", message = "Limit must be at least 1")
    public BigDecimal monthlyLimit;
    public BigDecimal spent = BigDecimal.ZERO;
    public double percentageUsed;
    public String status;
    public Integer month;
    public Integer year;
}
