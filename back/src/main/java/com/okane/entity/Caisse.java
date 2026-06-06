package com.okane.entity;


import com.okane.entity.enums.StatutCaisse;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "caisse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caisse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "solde_courant", nullable = false, precision = 19, scale = 2)
    private BigDecimal soldeCourant;

    @Column(name = "date_ouverture",nullable = false)
    private LocalDateTime dateOuverture;

    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCaisse statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_id", nullable = false)
    private Agence agence;
}