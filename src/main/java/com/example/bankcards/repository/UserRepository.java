package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmailForUpdate(String email);

    @Query("SELECT c FROM Card c WHERE c.cardHolder.email = ?1")
    List<Card> findCardsByEmail(String email, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.cardHolder.email = ?1 AND c.id = ?2")
    Optional<Card> findCardByEmail(String email, Long cardId);
}
