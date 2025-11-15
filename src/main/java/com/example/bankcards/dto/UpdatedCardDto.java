package com.example.bankcards.dto;

import com.example.bankcards.util.CardStatus;
import com.example.bankcards.util.customAnnotation.ValidCardStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UpdatedCardDto {
    @NotNull(message = "Id can't be empty")
    private Long id;
    @Pattern(regexp = "^\\d{4} \\d{4} \\d{4} \\d{4}$",message = "Card number doesn't match the form")
    private String cardNumber;
    @Positive(message = "User id must be > 0")
    private Long userId;
    private LocalDate expirationDate;
    @ValidCardStatus
    private CardStatus cardStatus;
    private BigDecimal balance;
}
