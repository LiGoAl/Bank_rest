package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockCardRequestDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.IdDto;
import com.example.bankcards.dto.TransferDto;
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
    public List<CardDto> readUserCards(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "5") int size) {
        return userCardService.readUserCards(page, size);
    }

    @GetMapping("/card")
    public CardDto readUserCard(@Valid @RequestBody IdDto idDto) {
        return userCardService.readUserCard(idDto.getId());
    }

    @PostMapping("/transfer")
    public void transferMoney(@Valid @RequestBody TransferDto transferDto) {
        userCardService.transferMoney(transferDto);
    }

    @PostMapping("/block")
    public void blockCardRequest(@Valid @RequestBody IdDto idDto) {
        userCardService.blockCardRequest(idDto.getId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/block-requests")
    public List<BlockCardRequestDto> readBlockCardRequests(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "5") int size) {
        return userCardService.readBlockCardRequests(page, size);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/block-requests")
    public void blockCard(@Valid @RequestBody IdDto idDto) {
        userCardService.blockCard(idDto.getId());
    }
}
