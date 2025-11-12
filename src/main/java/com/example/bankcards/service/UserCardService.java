package com.example.bankcards.service;

import com.example.bankcards.dto.BlockCardRequestDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.BlockCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.BlockCardRequestRepository;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.util.CardStatus;
import com.example.bankcards.util.mapper.BlockCardRequestMapper;
import com.example.bankcards.util.mapper.CardMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCardService {

    private final BlockCardRequestRepository blockCardRequestRepository;
    private final UserService userService;
    private final CardService cardService;

    public List<CardDto> readUserCards(Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = customUserDetails.getUsername();
        return userService.readUserCards(email, pageRequest).stream()
                .map(cardService::formatCardDtoCardNumber)
                .collect(Collectors.toList());
    }

    public CardDto readUserCard(Long cardId) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = customUserDetails.getUsername();
        return cardService.formatCardDtoCardNumber(userService.readUserCard(email, cardId));
    }

    public void transferMoney(TransferDto transferDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = customUserDetails.getUsername();
        List<CardDto> cardDtos = userService.readUserCards(email);
        validateUserCardsNumber(cardDtos, transferDto);
        validateTransfer(transferDto);
        cardService.transfer(transferDto);
    }

    public void blockCardRequest(Long cardId) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = customUserDetails.getUsername();
        Card card = CardMapper.INSTANCE.cardDtoToCard(userService.readUserCard(email, cardId));
        User user = userService.findByEmail(email);
        BlockCardRequest blockCardRequest = new BlockCardRequest(null, user, card, LocalDateTime.now(), false);
        blockCardRequestRepository.save(blockCardRequest);
    }

    public List<BlockCardRequestDto> readBlockCardRequests(Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<BlockCardRequest> requests = blockCardRequestRepository.findByProcessedFalse(pageRequest).getContent();
        return requests.stream()
                .map(BlockCardRequestMapper.INSTANCE::blockCardRequestToBlockCardRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void blockCard(Long requestId) {
        BlockCardRequest request = validateRequestId(requestId);
        request.setProcessed(true);
        request.getCard().setCardStatus(CardStatus.BLOCKED);
    }

    private void validateUserCardsNumber(List<CardDto> cardDtos, TransferDto transferDto) {

        if (transferDto.getFromCardNumber().equals(transferDto.getToCardNumber())) {
            throw new IllegalArgumentException("Card number must be different");
        }

        List<String> cardNumbersToValidate = List.of(
                transferDto.getFromCardNumber(),
                transferDto.getToCardNumber()
        );

        boolean allCardsExist = cardNumbersToValidate.stream()
                .allMatch(cardNumberToValidate ->
                        cardDtos.stream()
                                .map(CardDto::getCardNumber)
                                .anyMatch(cardNumber -> cardNumber.equals(cardNumberToValidate))
                );

        if (!allCardsExist) {
            throw new IllegalArgumentException("One or both card numbers do not exist in the user's cards.");
        }
    }

    private void validateTransfer(TransferDto transferDto) {
        Card cardFrom = cardService.findCardByCardNumber(transferDto.getFromCardNumber());
        if (cardFrom.getBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new IllegalArgumentException("On balance not enough money for transfer");
        }
    }

    private BlockCardRequest validateRequestId(Long requestId) {
        return blockCardRequestRepository.findById(requestId).orElseThrow(() -> new ResourceNotFoundException("Request id not found by id=%s".formatted(requestId)));
    }
}
