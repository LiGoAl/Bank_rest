package com.example.bankcards.entity;

import com.example.bankcards.util.CardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "card")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Card {
    @Id
    @SequenceGenerator(
            name = "card_sequence",
            sequenceName = "card_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "card_sequence"
    )
    private Long id;
    private String cardNumber;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User cardHolder;
    private LocalDate expirationDate;
    @Enumerated(EnumType.STRING)
    private CardStatus cardStatus;
    private BigDecimal balance;
}
