package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockCardRequestDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.exception.RequestValidationException;
import com.example.bankcards.service.UserCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user_cards")
@Validated
@RequiredArgsConstructor
public class UserCardController {

    private final UserCardService userCardService;

    @GetMapping
    public List<CardDto> readUserCards(@RequestParam(defaultValue = "0") Integer page,
                                       @RequestParam(defaultValue = "5") Integer size) {
        return userCardService.readUserCards(page, size);
    }

    @GetMapping("/{cardId}")
    public CardDto readUserCard(@PathVariable Long cardId) {
        return userCardService.readUserCard(validateId(cardId));
    }

    @PostMapping("/transfer")
    public void transferMoney(@Valid @RequestBody TransferDto transferDto) {
        userCardService.transferMoney(transferDto);
    }

    @PostMapping("/block/{cardId}")
    public void blockCardRequest(@PathVariable Long cardId) {
        userCardService.blockCardRequest(validateId(cardId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/block-requests")
    public List<BlockCardRequestDto> readBlockCardRequests(@RequestParam(defaultValue = "0") Integer page,
                                                           @RequestParam(defaultValue = "5") Integer size) {
        return userCardService.readBlockCardRequests(page, size);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/block-requests/{requestId}")
    public void blockCard(@PathVariable Long requestId) {
        userCardService.blockCard(validateId(requestId));
    }

    private Long validateId(Long id) {
        if (id == null || id <= 0) {
            throw new RequestValidationException("Id must be greater than 0 and can't be empty");
        } else return id;
    }
}
