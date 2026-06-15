package com.okane.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "corridor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Corridor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "taux_change", nullable = false, precision = 19, scale = 6)
    private BigDecimal tauxChange;

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private Boolean actif = true;

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

    @OneToMany(mappedBy = "corridor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TauxChangeHistorique> historiqueTaux;

    @OneToMany(mappedBy = "corridor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GrilleTarifaire> grilles;
}