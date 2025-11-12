package com.example.bankcards.dto;

import com.example.bankcards.util.CardStatus;
import com.example.bankcards.util.customAnnotation.ValidCardStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CardDto {
    @Null(message = "Id must be empty")
    private Long id;
    @NotNull(message = "Card number can't be empty")
    @Pattern(regexp = "^\\d{4} \\d{4} \\d{4} \\d{4}$",message = "Card number doesn't match the form")
    private String cardNumber;
    @NotNull(message = "User id can't be empty")
    @Positive(message = "User id must be > 0")
    private Long userId;
    @NotNull(message = "Expiration date can't be empty")
    private LocalDate expirationDate;
    @ValidCardStatus
    private CardStatus cardStatus;
    @NotNull(message = "Balance can't be empty")
    private BigDecimal balance;
}
