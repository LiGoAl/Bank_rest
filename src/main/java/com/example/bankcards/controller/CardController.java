package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.IdDto;
import com.example.bankcards.dto.UpdatedCardDto;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@Validated
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public List<CardDto> readCards(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size) {
        return cardService.readCards(page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardDto createCard(@Valid @RequestBody CardDto cardDto) {
        return cardService.createCard(cardDto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@Valid @RequestBody IdDto idDto) {
        cardService.deleteCard(idDto.getId());
    }

    @PutMapping
    public void updateCard(@Valid @RequestBody UpdatedCardDto updatedCardDto) {
        cardService.updateCard(updatedCardDto);
    }
}
