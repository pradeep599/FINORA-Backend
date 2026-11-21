package com.finpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AccountDto {
    public Long id;
    @NotBlank(message = "Account name is required")
    public String name;
    @NotBlank(message = "Account type is required")
    public String type;
    @NotNull(message = "Balance is required")
    public BigDecimal balance;
}
