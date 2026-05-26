package com.okane.pricing_configuration.bean;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "taux_change_historique")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TauxChangeHistorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "taux_ancien", nullable = false, precision = 19, scale = 6)
    private BigDecimal tauxAncien;

    @Column(name = "taux_nouveau", nullable = false, precision = 19, scale = 6)
    private BigDecimal tauxNouveau;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "date_changement", nullable = false)
    private LocalDateTime dateChangement;

    // historique appartient à un corridor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;
}
