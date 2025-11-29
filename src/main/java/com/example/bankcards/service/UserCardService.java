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

    public List<CardDto> readUserCards(int page, int size) {
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
        cardService.transfer(transferDto, email);
    }

    public void blockCardRequest(Long cardId) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = customUserDetails.getUsername();
        Card card = CardMapper.INSTANCE.cardDtoToCard(userService.readUserCard(email, cardId));
        User user = userService.findByEmail(email);
        BlockCardRequest blockCardRequest = new BlockCardRequest(null, user, card, LocalDateTime.now(), false);
        blockCardRequestRepository.save(blockCardRequest);
    }

    public List<BlockCardRequestDto> readBlockCardRequests(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<BlockCardRequest> requests = blockCardRequestRepository.findByProcessedFalse(pageRequest).getContent();
        return requests.stream()
                .map(BlockCardRequestMapper.INSTANCE::blockCardRequestToBlockCardRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void blockCard(Long requestId) {
        BlockCardRequest request = validateRequestId(requestId);
        if (request.getProcessed().equals(Boolean.TRUE)) {
            return;
        }
        Card lockedCard = cardService.findByCardNumberForUpdate(request.getCard().getCardNumber());
        lockedCard.setCardStatus(CardStatus.BLOCKED);
        request.setProcessed(true);
    }

    private BlockCardRequest validateRequestId(Long requestId) {
        return blockCardRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found by id=%s".formatted(requestId)));
    }
}
