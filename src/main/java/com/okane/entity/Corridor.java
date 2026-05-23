package com.okane.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "corridor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Corridor {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "taux_change", nullable = false, precision = 19, scale = 6)
    private BigDecimal tauxChange;

    @Column(nullable = false)
    private Boolean actif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pays_origine_id", nullable = false)
    private Pays paysOrigine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pays_destination_id", nullable = false)
    private Pays paysDestination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_source_id", nullable = false)
    private Devise deviseSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_destination_id", nullable = false)
    private Devise deviseDestination;
}