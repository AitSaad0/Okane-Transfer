package com.okane.dto.responseDto;

import com.okane.entity.enums.StatutCaisse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data  // ← Lombok génère TOUS les getters/setters automatiquement
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaisseResponseDTO {

    private Long id;
    private StatutCaisse statut;  // ← Ajouté
    private LocalDate dateCaisse;  // ← Changé de LocalDateTime à LocalDate
    private LocalDateTime dateOuverture;  // ← Ajouté
    private LocalDateTime dateClotureFaite;  // ← Ajouté
    private BigDecimal soldeOuverture;
    private BigDecimal soldeCourant;
    private BigDecimal soldeCloture;
    private BigDecimal soldeTheorique;
    private BigDecimal ecart;
    private String motifEcart;
    private BigDecimal totalEncaissements;
    private BigDecimal totalDecaissements;
    private Integer nombreOperations;
    private String agentNom;
    private String agentPrenom;
    private String agenceNom;
    private List<OperationCaisseDTO> operations;

    // Nested DTO pour les opérations
    @Data  // ← Lombok pour la classe interne aussi
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationCaisseDTO {
        private Long transfertId;
        private String codeRetrait;
        private String type;
        private BigDecimal montant;
        private String deviseCode;
        private LocalDateTime dateHeure;
        private String expediteurOuBeneficiaire;
    }
}