package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.exception.RequestValidationException;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cards")
@Validated
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public List<CardDto> readCards(@RequestParam(defaultValue = "0") Integer page,
                                  @RequestParam(defaultValue = "5") Integer size) {
        return cardService.readCards(page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardDto createCard(@Valid @RequestBody CardDto cardDto) {
        return cardService.createCard(cardDto);
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable("cardId") Long id) {
        cardService.deleteCard(validateCardId(id));
    }

    @PutMapping("/{cardId}")
    public void updateCard(@PathVariable("cardId") Long id,
                           @RequestParam(value = "cardNumber", required = false) String cardNumber,
                           @RequestParam(value = "userId", required = false) Long userId,
                           @RequestParam(value = "expirationDate", required = false) LocalDate expirationDate,
                           @RequestParam(value = "cardStatus", required = false) CardStatus cardStatus,
                           @RequestParam(value = "balance", required = false) BigDecimal balance) {
        cardService.updateCard(validateCardId(id),
                validateCardNumber(cardNumber),
                validateCardUserId(userId),
                expirationDate,
                validateCardStatus(cardStatus),
                balance);
    }

    private String validateCardNumber(String cardNumber) {
        if (cardNumber != null && !cardNumber.matches("^\\d{4} \\d{4} \\d{4} \\d{4}$")) {
            throw new RequestValidationException("Card number doesn't match the form");
        } else return cardNumber;
    }

    private Long validateCardUserId(Long userId) {
        if (userId != null && userId <= 0) {
            throw new RequestValidationException("User id must be > 0");
        } else return userId;
    }

    private CardStatus validateCardStatus(CardStatus cardStatus) {
        if (cardStatus != null) {
            try {
                CardStatus.valueOf(cardStatus.name());
                return cardStatus;
            } catch (IllegalArgumentException e) {
                throw new RequestValidationException("Card status must be ACTIVE, BLOCKED or EXPIRED");
            }
        } else {
            return cardStatus;
        }
    }

    private Long validateCardId(Long id) {
        if (id == null || id <= 0) {
            throw new RequestValidationException("Id must be greater than 0 and can't be empty");
        } else return id;
    }
}
