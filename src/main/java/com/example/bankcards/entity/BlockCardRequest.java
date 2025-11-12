package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "request")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BlockCardRequest {
    @Id
    @SequenceGenerator(
            name = "request_sequence",
            sequenceName = "request_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "request_sequence"
    )
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;
    private LocalDateTime requestDate;
    private Boolean processed;
}
