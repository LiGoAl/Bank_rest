package com.example.bankcards;

import com.example.bankcards.dto.BlockCardRequestDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.BlockCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.BlockCardRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserCardService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.CardStatus;
import com.example.bankcards.util.mapper.BlockCardRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserCardServiceTest {

    @Mock
    private BlockCardRequestRepository blockCardRequestRepository;

    @Mock
    private UserService userService;

    @Mock
    private CardService cardService;

    @InjectMocks
    private UserCardService userCardService;

    private CustomUserDetails customUserDetails;
    private String userEmail;
    private TransferDto transferDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customUserDetails = mock(CustomUserDetails.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(customUserDetails, null));
        userEmail = "test@example.com";
        transferDto = new TransferDto("1234 5678 9012 3456", "1234 5678 9012 9999", BigDecimal.valueOf(100));
    }

    @Test
    public void testReadUserCards() {
        when(customUserDetails.getUsername()).thenReturn(userEmail);

        CardDto card1 = new CardDto(1L, "1234 5678 9012 3456", 1L, LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(2000));
        CardDto card2 = new CardDto(2L, "1234 5678 9012 9999", 1L, LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(3000));
        List<CardDto> cardDtos = Arrays.asList(card1, card2);

        when(userService.readUserCards(userEmail, PageRequest.of(0, 10))).thenReturn(cardDtos);
        when(cardService.formatCardDtoCardNumber(any(CardDto.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        List<CardDto> result = userCardService.readUserCards(0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(cardDtos.get(0), result.get(0));
        assertSame(cardDtos.get(1), result.get(1));

        verify(userService).readUserCards(userEmail, PageRequest.of(0, 10));
        verify(cardService).formatCardDtoCardNumber(card1);
        verify(cardService).formatCardDtoCardNumber(card2);
    }

    @Test
    public void testReadUserCard() {
        Long cardId = 1L;

        when(customUserDetails.getUsername()).thenReturn(userEmail);

        CardDto testCard = new CardDto(1L, "1234 5678 9012 3456", 1L, LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(2000));

        when(userService.readUserCard(userEmail, cardId)).thenReturn(testCard);
        when(cardService.formatCardDtoCardNumber(testCard)).thenReturn(testCard);

        CardDto result = userCardService.readUserCard(cardId);

        assertNotNull(result);
        assertEquals(testCard, result);

        verify(userService).readUserCard(userEmail, cardId);
        verify(cardService).formatCardDtoCardNumber(testCard);
    }

    @Test
    public void testTransferMoney_Successful() {
        when(customUserDetails.getUsername()).thenReturn(userEmail);

        doNothing().when(cardService).transfer(transferDto, userEmail);

        userCardService.transferMoney(transferDto);

        verify(cardService).transfer(transferDto, userEmail);
    }

    @Test
    public void testBlockCardRequest_Successful() {
        User user = new User();
        user.setEmail(userEmail);
        user.setId(1L);

        Card card = new Card(1L, "1234 5678 9012 3456", user, LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(200));
        user.setCards(List.of(card));

        CardDto cardDto = new CardDto();
        cardDto.setCardNumber(card.getCardNumber());
        cardDto.setUserId(card.getCardHolder().getId());
        cardDto.setCardStatus(card.getCardStatus());
        cardDto.setBalance(card.getBalance());
        cardDto.setExpirationDate(card.getExpirationDate());

        BlockCardRequest expectedRequest = new BlockCardRequest(1L, user, card, LocalDateTime.now(), false);


        when(userService.findByEmail(userEmail)).thenReturn(user);
        when(userService.readUserCard(userEmail, card.getId())).thenReturn(cardDto);
        when(blockCardRequestRepository.save(any(BlockCardRequest.class))).thenReturn(expectedRequest);

        userCardService.blockCardRequest(card.getId());

        verify(blockCardRequestRepository).save(any(BlockCardRequest.class));
    }

    @Test
    public void testReadBlockCardRequests() {
        User user = new User();
        user.setEmail(userEmail);
        user.setId(1L);

        Card card = new Card(1L, "1234 5678 9012 3456", user, LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(200));
        user.setCards(List.of(card));

        BlockCardRequest request1 = new BlockCardRequest(1L, user, card, LocalDateTime.now(), false);
        BlockCardRequest request2 = new BlockCardRequest(2L, user, card, LocalDateTime.now(), false);
        List<BlockCardRequest> requests = Arrays.asList(request1, request2);

        Page<BlockCardRequest> page = new PageImpl<>(requests);
        when(blockCardRequestRepository.findByProcessedFalse(any(PageRequest.class))).thenReturn(page);

        List<BlockCardRequestDto> result = userCardService.readBlockCardRequests(0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(blockCardRequestRepository).findByProcessedFalse(PageRequest.of(0, 10));
    }

    @Test
    public void testBlockCard_Successful() {
        User user = new User();
        user.setEmail(userEmail);
        user.setId(1L);

        Card card = new Card(1L, "1234 5678 9012 3456", user, LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(200));
        user.setCards(List.of(card));

        BlockCardRequest blockedCardRequest = new BlockCardRequest(1L, user, card, LocalDateTime.now(), false);

        when(blockCardRequestRepository.findByIdForUpdate(blockedCardRequest.getId())).thenReturn(Optional.of(blockedCardRequest));
        when(cardService.findByCardNumberForUpdate(card.getCardNumber())).thenReturn(card);

        userCardService.blockCard(blockedCardRequest.getId());

        assertEquals(CardStatus.BLOCKED, card.getCardStatus());
        assertTrue(blockedCardRequest.getProcessed());
    }

    @Test
    public void testBlockCard_RequestAlreadyProcessed() {
        User user = new User();
        user.setEmail(userEmail);
        user.setId(1L);

        Card card = new Card(1L, "1234 5678 9012 3456", user, LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(200));
        user.setCards(List.of(card));

        BlockCardRequest blockedCardRequest = new BlockCardRequest(1L, user, card, LocalDateTime.now(), true);

        when(blockCardRequestRepository.findByIdForUpdate(blockedCardRequest.getId())).thenReturn(Optional.of(blockedCardRequest));

        userCardService.blockCard(blockedCardRequest.getId());

        assertEquals(CardStatus.ACTIVE, card.getCardStatus());
        assertTrue(blockedCardRequest.getProcessed());

        verify(cardService, never()).findByCardNumberForUpdate(anyString());
    }

    @Test
    public void testBlockCard_RequestNotFound() {
        Long requestId = 3L;

        when(blockCardRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userCardService.blockCard(requestId));
    }
}
