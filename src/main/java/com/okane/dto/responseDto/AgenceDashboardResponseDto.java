package com.okane.dto.responseDto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AgenceDashboardResponseDto {

    // Identification
    private Long       agenceId;
    private String     agenceNom;
    private String     agenceVille;
    private String     paysNom;

    // Volume quotidien
    private BigDecimal volumeEnvoiJour;       // total montants envoyés aujourd'hui
    private BigDecimal volumePaiementJour;    // total montants payés aujourd'hui
    private Long       nombreTransfertJour;   // nb opérations du jour

    // Taux de succès
    private Double     tauxSucces;            // % transferts PAYE / total non ANNULE
    private Long       transfertsPaye;        // nb PAYE
    private Long       transfertsEnAttente;   // nb EN_ATTENTE
    private Long       transfertsAnnule;      // nb ANNULE
    private Long       transfertsExpire;      // nb EXPIRE

    // Commissions
    private BigDecimal commissionsGenerees;   // somme part agence sur transferts PAYE
    private BigDecimal plafondJournalier;     // plafond configuré
    private Double     tauxUtilisationPlafond; // volumeEnvoiJour / plafondJournalier * 100

    // Caisse
    private BigDecimal soldeCaisseActuel;     // soldeCourant de la caisse active
    private Boolean    caisseOuverte;         // true si une caisse est OUVERTE
}