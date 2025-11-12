package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransferDto {
    @NotNull(message = "Card number can't be empty")
    @Pattern(regexp = "^\\d{4} \\d{4} \\d{4} \\d{4}$",message = "Card number doesn't match the form")
    private String fromCardNumber;
    @NotNull(message = "Card number can't be empty")
    @Pattern(regexp = "^\\d{4} \\d{4} \\d{4} \\d{4}$",message = "Card number doesn't match the form")
    private String toCardNumber;
    @NotNull(message = "Amount can't be empty")
    @Positive(message = "Amount can't be <= 0")
    private BigDecimal amount;
}
