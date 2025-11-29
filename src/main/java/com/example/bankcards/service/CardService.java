package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.UpdatedCardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
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
    public void transfer(TransferDto transferDto, String email) {
        String fromNumber = transferDto.getFromCardNumber();
        String toNumber = transferDto.getToCardNumber();

        if (fromNumber.equals(toNumber)) {
            throw new IllegalArgumentException("Card number must be different");
        }

        String first = fromNumber.compareTo(toNumber) < 0 ? fromNumber : toNumber;
        String second = first.equals(fromNumber) ? toNumber : fromNumber;

        Card firstLocked = cardRepository.findByCardNumberForUpdate(first)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found by Card Number: %s".formatted(first)));
        Card secondLocked = cardRepository.findByCardNumberForUpdate(second)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found by Card Number: %s".formatted(second)));

        Card cardFrom = first.equals(fromNumber) ? firstLocked : secondLocked;
        Card cardTo = cardFrom.equals(firstLocked) ? secondLocked : firstLocked;

        User user = userService.findByEmailForUpdate(email);

        validateUserCardsNumber(user, transferDto);
        validateTransfer(transferDto, cardFrom, cardTo);

        cardFrom.setBalance(cardFrom.getBalance().subtract(transferDto.getAmount()));
        cardTo.setBalance(cardTo.getBalance().add(transferDto.getAmount()));
    }

    private void validateUserCardsNumber(User user, TransferDto transferDto) {
        List<String> cardNumbersToValidate = List.of(
                transferDto.getFromCardNumber(),
                transferDto.getToCardNumber()
        );

        boolean allCardsExist = cardNumbersToValidate.stream()
                .allMatch(cardNumberToValidate ->
                        user.getCards()
                                .stream()
                                .map(Card::getCardNumber)
                                .anyMatch(cardNumber -> cardNumber.equals(cardNumberToValidate))
                );

        if (!allCardsExist) {
            throw new IllegalArgumentException("One or both card numbers do not exist in the user's cards.");
        }
    }

    private void validateTransfer(TransferDto transferDto, Card cardFrom, Card cardTo) {
        if (!(cardFrom.getCardStatus().equals(CardStatus.ACTIVE) && cardTo.getCardStatus().equals(CardStatus.ACTIVE))) {
            throw new IllegalArgumentException("Card status must be ACTIVE");
        }
        if (cardFrom.getBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new IllegalArgumentException("On balance not enough money for transfer");
        }
    }

    public Card findByCardNumberForUpdate(String cardNumber) {
        return cardRepository.findByCardNumberForUpdate(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found by Card Number: %s".formatted(cardNumber)));
    }

    public CardDto formatCardDtoCardNumber(CardDto cardDto) {
        cardDto.setCardNumber(CardMask.formatCardNumber(cardDto.getCardNumber()));
        return cardDto;
    }
}
