package com.okane.entity;

import com.okane.entity.enums.TypeToken;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "valeur", nullable = false, unique = true, columnDefinition = "TEXT")
    private String valeur;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypeToken type;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Column(name = "booleen_utilise", nullable = false)
    private Boolean booleenUtilise = false;

    // N tokens → 1 utilisateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

}
