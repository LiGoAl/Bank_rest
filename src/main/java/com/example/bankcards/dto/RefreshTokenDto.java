package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class RefreshTokenDto {
    @NotNull(message = "Refresh token can't be empty")
    private String refreshToken;
}
