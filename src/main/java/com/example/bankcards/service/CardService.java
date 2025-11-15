package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.UpdatedCardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.ResourceAlreadyOccupiedException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardMask;
import com.example.bankcards.util.CardStatus;
import com.example.bankcards.util.mapper.CardMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserService userService;

    public List<CardDto> readCards(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Card> cards = cardRepository.findAll(pageRequest).getContent();
        return cards.stream()
                .map(CardMapper.INSTANCE::cardToCardDto)
                .map(this::formatCardDtoCardNumber)
                .collect(Collectors.toList());
    }

    @Transactional
    public CardDto createCard(CardDto cardDto) {
        Card card = CardMapper.INSTANCE.cardDtoToCard(cardDto);
        validateCardNumber(cardDto);
        card.setCardHolder(userService.validateUserId(cardDto.getUserId()));
        Card savedCard = cardRepository.save(card);
        return formatCardDtoCardNumber(CardMapper.INSTANCE.cardToCardDto(savedCard));
    }

    public void deleteCard(Long id) {
        cardRepository.deleteById(validateCardId(id).getId());
    }

    @Transactional
    public void updateCard(UpdatedCardDto updatedCardDto) {
        cardRepository.findById(validateCardId(updatedCardDto.getId()).getId()).map(card ->
                validateCard(card,
                        updatedCardDto.getCardNumber(),
                        updatedCardDto.getUserId(),
                        updatedCardDto.getExpirationDate(),
                        updatedCardDto.getCardStatus(),
                        updatedCardDto.getBalance()));
    }

    private void validateCardNumber(CardDto cardDto) {
        cardRepository.findByCardNumber(cardDto.getCardNumber()).ifPresent(card -> {
            throw new ResourceAlreadyOccupiedException("Card number already taken");
        });
    }

    private Card validateCardId(Long id) {
        return cardRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found by id=%s".formatted(id)));
    }

    private Card validateCard(Card card, String cardNumber, Long userId, LocalDate expirationDate, CardStatus cardStatus, BigDecimal balance) {
        if (cardNumber != null) card.setCardNumber(validateCardNumber(cardNumber, card));
        if (userId != null) card.setCardHolder(userService.validateUserId(userId));
        if (expirationDate != null) card.setExpirationDate(expirationDate);
        if (cardStatus != null) card.setCardStatus(cardStatus);
        if (balance != null) card.setBalance(balance);
        return card;
    }

    private String validateCardNumber(String cardNumber, Card card) {
        if (!card.getCardNumber().equals(cardNumber)) {
            cardRepository.findByCardNumber(cardNumber).ifPresent(cardOpt -> {
                throw new ResourceAlreadyOccupiedException("Card number already taken");
            });
        }
        return cardNumber;
    }

    @Transactional
    public void transfer(TransferDto transferDto) {
        Card cardFrom = findCardByCardNumber(transferDto.getFromCardNumber()),
                cardTo = findCardByCardNumber(transferDto.getToCardNumber());
        cardFrom.setBalance(cardFrom.getBalance().subtract(transferDto.getAmount()));
        cardTo.setBalance(cardTo.getBalance().add(transferDto.getAmount()));
    }

    public Card findCardByCardNumber(String cardNumber) {
        return cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found by Card Number: %s".formatted(cardNumber)));
    }

    public CardDto formatCardDtoCardNumber(CardDto cardDto) {
        cardDto.setCardNumber(CardMask.formatCardNumber(cardDto.getCardNumber()));
        return cardDto;
    }
}
