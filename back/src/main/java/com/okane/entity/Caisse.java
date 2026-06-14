package com.okane.entity;

import com.okane.entity.enums.StatutCaisse;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * FIXES APPLIED:
 * 1. Added missing field: dateCaisse (LocalDate) — used by CaisseRepository queries and CaisseServiceImpl
 * 2. Added missing field: soldeOuverture — referenced in calculerSoldeTheorique()
 * 3. Added missing field: soldeCloture — set at close time
 * 4. Added missing field: soldeTheorique — computed value stored at close
 * 5. Added missing field: ecart — discrepancy amount
 * 6. Added missing field: motifEcart — discrepancy reason
 * 7. Added missing field: totalEncaissements — daily in-flows
 * 8. Added missing field: totalDecaissements — daily out-flows
 * 9. Added missing field: ecartDetecte (boolean) — flag set automatically
 * 10. Added missing field: observation — optional note at close
 * 11. Added missing @OneToMany relation: transferts — referenced in toDTO(caisse, true)
 */
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

    /**
     * FIX 1: dateCaisse is required by:
     *   - CaisseRepository.findByAgentAndDateCaisseAndStatut()
     *   - CaisseRepository.findByAgenceIdAndDate()
     *   - CaisseResponseDTO.setDateCaisse()
     * Previously absent from the entity.
     */
    @Column(name = "date_caisse", nullable = false)
    private LocalDate dateCaisse;

    /**
     * FIX 2: soldeOuverture is required by calculerSoldeTheorique() in CaisseServiceImpl.
     * Previously absent from the entity.
     */
    @Column(name = "solde_ouverture", precision = 19, scale = 2)
    private BigDecimal soldeOuverture;

    @Column(name = "solde_courant", nullable = false, precision = 19, scale = 2)
    private BigDecimal soldeCourant;

    /**
     * FIX 3: soldeCloture is set in cloturerCaisse().
     * Previously absent from the entity.
     */
    @Column(name = "solde_cloture", precision = 19, scale = 2)
    private BigDecimal soldeCloture;

    /**
     * FIX 4: soldeTheorique is set in cloturerCaisse().
     * Previously absent from the entity.
     */
    @Column(name = "solde_theorique", precision = 19, scale = 2)
    private BigDecimal soldeTheorique;

    /**
     * FIX 5: ecart is set in both cloturerCaisse() and signalerEcart().
     * Previously absent from the entity.
     */
    @Column(name = "ecart", precision = 19, scale = 2)
    private BigDecimal ecart;

    /**
     * FIX 6: motifEcart is set in signalerEcart().
     * Previously absent from the entity.
     */
    @Column(name = "motif_ecart")
    private String motifEcart;

    /**
     * FIX 7: totalEncaissements referenced in calculerSoldeTheorique() and toDTO().
     * Previously absent from the entity.
     */
    @Column(name = "total_encaissements", precision = 19, scale = 2)
    private BigDecimal totalEncaissements;

    /**
     * FIX 8: totalDecaissements referenced in calculerSoldeTheorique() and toDTO().
     * Previously absent from the entity.
     */
    @Column(name = "total_decaissements", precision = 19, scale = 2)
    private BigDecimal totalDecaissements;

    /**
     * FIX 9: ecartDetecte flag set automatically when ecart > 0.01.
     * Previously absent from the entity.
     */
    @Column(name = "ecart_detecte", nullable = false)
    @Builder.Default
    private boolean ecartDetecte = false;

    /**
     * FIX 10: observation is an optional note at close time.
     * Previously absent from the entity.
     */
    @Column(name = "observation", length = 500)
    private String observation;

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

    /**
     * FIX 11: transferts relation is used in toDTO(caisse, true) to list daily operations.
     * The join column references caisse_id on the Transfert side.
     * mappedBy must match the field name in Transfert that holds the @ManyToOne Caisse reference.
     * Adjust "caisse" if the actual field name in Transfert differs.
     */
    @OneToMany
    @JoinColumn(name = "caisse_id")
    private List<Transfert> transferts;

    public Boolean getEcartDetecte() {
        return ecartDetecte;
    }

    public void setEcartDetecte(Boolean ecartDetecte) {
        this.ecartDetecte = ecartDetecte;
    }


    ;
}