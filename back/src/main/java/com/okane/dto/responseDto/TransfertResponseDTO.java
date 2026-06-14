package com.okane.dto.responseDto;

import com.okane.entity.enums.StatutTransfert;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransfertResponseDTO {
    private Long id;
    private String codeRetrait;
    private String codeTransaction;

    private String expediteurNom;
    private String expediteurPrenom;
    private String expediteurTelephone;
    private String expediteurEmail;

    private String beneficiaireNom;
    private String beneficiairePrenom;
    private String beneficiaireTelephone;
    private String beneficiaireEmail;

    private String deviseSource;
    private String deviseDestination;
    private BigDecimal montantEnvoye;
    private BigDecimal montantRecu;
    private BigDecimal frais;
    private BigDecimal tauxChange;

    private StatutTransfert statut;

    private Long agenceId;
    private String agenceNom;
    private Long agentId;
    private String agentNom;
    private String agentPrenom;

    private Long corridorId;
    private String corridorDescription;;

    private String motifAnnulation;
    private String annulePar;
    private LocalDateTime dateAnnulation;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime dateValidation;
    private LocalDateTime datePaiement;
    private LocalDateTime dateExpiration;

    private String notes;
    private boolean flagged;
    private String flagReason;

    private BigDecimal montantNet;


    @Override
    public String toString() {
        return "TransfertResponseDTO{" +
                "id=" + id +
                ", codeRetrait='" + codeRetrait + '\'' +
                ", codeTransaction='" + codeTransaction + '\'' +
                ", expediteurNom='" + expediteurNom + '\'' +
                ", expediteurPrenom='" + expediteurPrenom + '\'' +
                ", beneficiaireNom='" + beneficiaireNom + '\'' +
                ", beneficiairePrenom='" + beneficiairePrenom + '\'' +
                ", deviseSource='" + deviseSource + '\'' +
                ", deviseDestination='" + deviseDestination + '\'' +
                ", montantEnvoye=" + montantEnvoye +
                ", montantRecu=" + montantRecu +
                ", statut=" + statut +
                ", dateCreation=" + dateCreation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransfertResponseDTO that = (TransfertResponseDTO) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}