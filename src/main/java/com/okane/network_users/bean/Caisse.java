package com.okane.network_users.bean;

import com.okane.shared.StatutCaisse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "caisse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caisse {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "solde_courant", nullable = false, precision = 19, scale = 2)
    private BigDecimal soldeCourant;

    @Column(name = "date_ouverture", nullable = false)
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