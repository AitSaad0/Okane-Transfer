package com.okane.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "grille_tarifaire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrilleTarifaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "montant_min", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantMin;

    @Column(name = "montant_max", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantMax;

    @Column(name = "frais_fixe", nullable = false, precision = 19, scale = 2)
    private BigDecimal fraisFixe;

    @Column(name = "pourcentage_frais", nullable = false)
    private Double pourcentageFrais;

    @Column(name = "part_agence", nullable = false)
    private Double partAgence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;
}