package com.finpulse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDto {
    public Long id;
    @NotNull(message = "Account is required")
    public Long accountId;
    public String accountName;
    public Long categoryId;
    public String categoryName;
    public String categoryColor;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    public BigDecimal amount;
    @NotBlank(message = "Type is required")
    public String type;
    @NotBlank(message = "Description is required")
    public String description;
    @NotNull(message = "Transaction date is required")
    public LocalDate transactionDate;
    public boolean aiCategorized;
}
