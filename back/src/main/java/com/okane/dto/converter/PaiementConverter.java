package com.okane.dto.converter;

import com.okane.dto.responseDto.PaiementResponseDTO;
import com.okane.entity.Transfert;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PaiementConverter {

    public PaiementResponseDTO toPaiementResponseDTO(Transfert t) {
        if (t == null) return null;

        String corridorDescription = null;
        String deviseSource = null;
        String deviseDestination = null;
        if (t.getCorridor() != null) {
            if (t.getCorridor().getPaysOrigine() != null && t.getCorridor().getPaysDestination() != null) {
                corridorDescription = t.getCorridor().getPaysOrigine().getNom()
                        + " \u2192 " + t.getCorridor().getPaysDestination().getNom();
            }
            if (t.getCorridor().getDeviseSource() != null) {
                deviseSource = t.getCorridor().getDeviseSource().getCode();
            }
            if (t.getCorridor().getDeviseDestination() != null) {
                deviseDestination = t.getCorridor().getDeviseDestination().getCode();
            }
        }

        BigDecimal fraisTotal = t.getFrais() != null ? t.getFrais() : BigDecimal.ZERO;
        BigDecimal fraisVar = fraisTotal.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fraisFix = fraisTotal.subtract(fraisVar);

        BigDecimal montantRecu = t.getMontantNet() != null && t.getCorridor() != null && t.getCorridor().getTauxChange() != null
                ? t.getMontantNet().multiply(t.getCorridor().getTauxChange()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String expediteurNomComplet = t.getExpediteur() != null
                ? (t.getExpediteur().getPrenom() + " " + t.getExpediteur().getNom()).trim()
                : null;
        String expediteurPays = t.getExpediteur() != null && t.getExpediteur().getPays() != null
                ? t.getExpediteur().getPays().getNom() : null;

        String beneficiaireNomComplet = t.getBeneficiaire() != null
                ? (t.getBeneficiaire().getPrenom() + " " + t.getBeneficiaire().getNom()).trim()
                : null;
        String beneficiairePays = t.getBeneficiaire() != null && t.getBeneficiaire().getPays() != null
                ? t.getBeneficiaire().getPays().getNom() : null;

        return PaiementResponseDTO.builder()
                .id(t.getId())
                .codeRetrait(t.getCodeRetrait())
                .reference("TRF-" + String.format("%06d", t.getId()))
                .corridorDescription(corridorDescription)
                .dateEnvoi(t.getDateCreation())
                .statut(t.getStatut() != null ? t.getStatut().name() : null)
                .expediteurNomComplet(expediteurNomComplet)
                .expediteurTelephone(t.getExpediteur() != null ? t.getExpediteur().getTelephone() : null)
                .expediteurEmail(t.getExpediteur() != null ? t.getExpediteur().getEmail() : null)
                .expediteurPieceIdentite(t.getExpediteur() != null ? t.getExpediteur().getNumPieceIdentite() : null)
                .expediteurPays(expediteurPays)
                .beneficiaireNomComplet(beneficiaireNomComplet)
                .beneficiaireTelephone(t.getBeneficiaire() != null ? t.getBeneficiaire().getTelephone() : null)
                .beneficiairePays(beneficiairePays)
                .montantDepart(t.getMontantEnvoye())
                .fraisFixes(fraisFix)
                .fraisProportionnels(fraisVar)
                .totalFrais(fraisTotal)
                .montantNet(t.getMontantNet())
                .tauxChange(t.getCorridor() != null ? t.getCorridor().getTauxChange() : null)
                .sourceTaux("Corridor")
                .montantRecu(montantRecu)
                .deviseSource(deviseSource)
                .deviseDestination(deviseDestination)
                .dateCreation(t.getDateCreation())
                .datePaiement(t.getDatePaiement())
                .agentEnvoiNom(t.getAgentEnvoi() != null ? t.getAgentEnvoi().getNom() : null)
                .agentEnvoiPrenom(t.getAgentEnvoi() != null ? t.getAgentEnvoi().getPrenom() : null)
                .agentPaiementNom(t.getAgentPaiement() != null ? t.getAgentPaiement().getNom() : null)
                .agentPaiementPrenom(t.getAgentPaiement() != null ? t.getAgentPaiement().getPrenom() : null)
                .paye(t.getStatut() != null && "PAYE".equals(t.getStatut().name()))
                .build();
    }
}
