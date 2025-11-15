package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IdDto {
    @NotNull(message = "Id can't be empty")
    @Positive(message = "Id must be > 0")
    private Long id;
}
