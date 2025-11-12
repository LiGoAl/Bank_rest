package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BlockCardRequestDto {
    private Long id;
    private Long userId;
    private Long cardId;
    private LocalDateTime requestDate;
    private Boolean processed;
}
