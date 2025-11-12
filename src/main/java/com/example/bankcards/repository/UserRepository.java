package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT c FROM Card c WHERE c.cardHolder.email = ?1")
    List<Card> findCardsByEmail(String email, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.cardHolder.email = ?1")
    List<Card> findCardsByEmail(String email);

    @Query("SELECT c FROM Card c WHERE c.cardHolder.email = ?1 AND c.id = ?2")
    Optional<Card> findCardByEmail(String email, Long cardId);
}
