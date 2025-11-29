package com.example.bankcards;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.UpdatedCardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.CardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CardService cardService;

    private Card existingCard;
    private CardDto cardDto;
    private UpdatedCardDto updatedCardDto;
    private String userEmail;
    private TransferDto transferDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingCard = new Card();
        existingCard.setId(1L);
        existingCard.setCardNumber("1234 5678 9012 3456");
        existingCard.setCardHolder(new User());
        existingCard.setExpirationDate(LocalDate.now().plusYears(1));
        existingCard.setCardStatus(CardStatus.ACTIVE);
        existingCard.setBalance(BigDecimal.valueOf(1000));

        cardDto = new CardDto();
        cardDto.setCardNumber("1234 5678 9012 3456");
        cardDto.setUserId(1L);
        cardDto.setCardStatus(CardStatus.ACTIVE);
        cardDto.setBalance(BigDecimal.valueOf(1000));
        cardDto.setExpirationDate(existingCard.getExpirationDate());

        updatedCardDto = new UpdatedCardDto();
        updatedCardDto.setId(1L);
        updatedCardDto.setCardNumber("1234 5678 9012 3456");
        updatedCardDto.setUserId(1L);
        updatedCardDto.setExpirationDate(LocalDate.now().plusYears(2));
        updatedCardDto.setCardStatus(CardStatus.ACTIVE);
        updatedCardDto.setBalance(BigDecimal.valueOf(2000));

        userEmail = "test@example.com";
        transferDto = new TransferDto("1234 5678 9012 3456", "1234 5678 9012 9999", BigDecimal.valueOf(100));
    }

    @Test
    void testUpdateCardSuccess() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(existingCard));
        when(userService.validateUserId(updatedCardDto.getUserId())).thenReturn(new User());

        cardService.updateCard(updatedCardDto);

        assertEquals(LocalDate.now().plusYears(2), existingCard.getExpirationDate());
        assertEquals(BigDecimal.valueOf(2000), existingCard.getBalance());
        verify(cardRepository, times(2)).findById(1L);
    }

    @Test
    void testUpdateCardWithNonExistentId() {
        when(cardRepository.findById(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            updatedCardDto.setId(2L);
            cardService.updateCard(updatedCardDto);
        });

        assertEquals("Card not found by id=2", exception.getMessage());
        verify(cardRepository, times(1)).findById(2L);
    }

    @Test
    void testUpdateCardWithInvalidUserId() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(existingCard));
        when(userService.validateUserId(updatedCardDto.getUserId())).thenThrow(new ResourceNotFoundException("User not found"));

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            cardService.updateCard(updatedCardDto);
        });

        assertEquals("User not found", exception.getMessage());
        verify(cardRepository, times(2)).findById(1L);
    }

    @Test
    void testReadCards() {
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(existingCard));
        when(cardRepository.findAll(any(PageRequest.class))).thenReturn(cardPage);

        List<CardDto> result = cardService.readCards(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("**** **** **** 3456", result.get(0).getCardNumber());
        verify(cardRepository).findAll(any(PageRequest.class));
    }

    @Test
    void testReadCardsEmptyResult() {
        Page<Card> emptyPage = new PageImpl<>(Collections.emptyList());
        when(cardRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        List<CardDto> result = cardService.readCards(0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cardRepository).findAll(any(PageRequest.class));
    }

    @Test
    void testCreateCardSuccess() {
        when(userService.validateUserId(cardDto.getUserId())).thenReturn(existingCard.getCardHolder());
        when(cardRepository.findByCardNumber(cardDto.getCardNumber())).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenReturn(existingCard);

        CardDto result = cardService.createCard(cardDto);

        assertNotNull(result);
        assertEquals("**** **** **** 3456", result.getCardNumber());
        verify(cardRepository).save(any(Card.class));
        verify(userService).validateUserId(cardDto.getUserId());
    }

    @Test
    void testCreateCardWithInvalidNumber() {
        doThrow(new IllegalArgumentException("Invalid card number")).when(cardRepository).findByCardNumber(cardDto.getCardNumber());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cardService.createCard(cardDto);
        });

        assertEquals("Invalid card number", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testCreateCardWithNonExistentUser() {
        when(userService.validateUserId(cardDto.getUserId())).thenThrow(new ResourceNotFoundException("User not found"));

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            cardService.createCard(cardDto);
        });

        assertEquals("User not found", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    public void testDeleteCardSuccess() {
        when(cardRepository.findById(existingCard.getId())).thenReturn(Optional.of(existingCard));

        cardService.deleteCard(existingCard.getId());

        verify(cardRepository).deleteById(existingCard.getId());
    }

    @Test
    public void testDeleteCardNotFound() {
        when(cardRepository.findById(existingCard.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cardService.deleteCard(existingCard.getId());
        });

        assertEquals("Card not found by id=1", exception.getMessage());

        verify(cardRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testTransfer_Successful() {
        User user = new User();
        user.setEmail(userEmail);
        user.setId(1L);

        Card cardFrom = new Card(1L, "1234 5678 9012 3456", user, LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(200));
        Card cardTo = new Card(2L, "1234 5678 9012 9999", user, LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(50));
        user.setCards(List.of(cardFrom, cardTo));

        when(cardRepository.findByCardNumberForUpdate("1234 5678 9012 3456")).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findByCardNumberForUpdate("1234 5678 9012 9999")).thenReturn(Optional.of(cardTo));
        when(userService.findByEmailForUpdate(userEmail)).thenReturn(user);

        cardService.transfer(transferDto, userEmail);

        assertEquals(BigDecimal.valueOf(100), cardFrom.getBalance());
        assertEquals(BigDecimal.valueOf(150), cardTo.getBalance());

        verify(cardRepository).findByCardNumberForUpdate("1234 5678 9012 3456");
        verify(cardRepository).findByCardNumberForUpdate("1234 5678 9012 9999");
        verify(userService).findByEmailForUpdate(userEmail);
    }

    @Test
    public void testTransfer_SameCardNumber_ThrowsException() {
        transferDto.setToCardNumber(transferDto.getFromCardNumber());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> cardService.transfer(transferDto, userEmail));

        assertEquals("Card number must be different", e.getMessage());
    }

    @Test
    public void testTransfer_OneCardNotFound_ThrowsException() {
        when(cardRepository.findByCardNumberForUpdate("1234 5678 9012 3456")).thenReturn(Optional.empty());

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, () -> cardService.transfer(transferDto, userEmail));

        assertEquals("Card not found by Card Number: 1234 5678 9012 3456", e.getMessage());
    }

    @Test
    public void testTransfer_NotEnoughBalance_ThrowsException() {
        User user = new User();
        user.setEmail(userEmail);
        user.setId(1L);

        Card cardFrom = new Card(1L, "1234 5678 9012 3456", user, LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(20));
        Card cardTo = new Card(2L, "1234 5678 9012 9999", user, LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(50));
        user.setCards(List.of(cardFrom, cardTo));

        when(cardRepository.findByCardNumberForUpdate("1234 5678 9012 3456")).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findByCardNumberForUpdate("1234 5678 9012 9999")).thenReturn(Optional.of(cardTo));
        when(userService.findByEmailForUpdate(userEmail)).thenReturn(user);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> cardService.transfer(transferDto, userEmail));

        assertEquals("On balance not enough money for transfer", e.getMessage());
    }

    @Test
    public void testTransfer_InactiveCard_ThrowsException() {
        User user = new User();
        user.setEmail(userEmail);
        user.setId(1L);

        Card cardFrom = new Card(1L, "1234 5678 9012 3456", user, LocalDate.now().plusYears(2), CardStatus.BLOCKED, BigDecimal.valueOf(200));
        Card cardTo = new Card(2L, "1234 5678 9012 9999", user, LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(50));
        user.setCards(List.of(cardFrom, cardTo));

        when(cardRepository.findByCardNumberForUpdate("1234 5678 9012 3456")).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findByCardNumberForUpdate("1234 5678 9012 9999")).thenReturn(Optional.of(cardTo));
        when(userService.findByEmailForUpdate(userEmail)).thenReturn(user);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> cardService.transfer(transferDto, userEmail));

        assertEquals("Card status must be ACTIVE", e.getMessage());
    }
}
