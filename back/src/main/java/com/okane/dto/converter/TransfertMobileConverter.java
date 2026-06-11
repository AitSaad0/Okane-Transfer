package com.okane.dto.converter;

import com.okane.dto.responseDto.TransfertMobileResponseDTO;
import com.okane.entity.Transfert;
import com.okane.entity.TransfertMobile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TransfertMobileConverter {

    public TransfertMobileResponseDTO toResponseDTO(Transfert transfert, TransfertMobile mobile,
                                                     BigDecimal fraisFixe, BigDecimal fraisVariable) {
        if (transfert == null) return null;

        String corridorDescription = null;
        String deviseSourceCode = null;
        String deviseDestinationCode = null;
        BigDecimal tauxChange = null;

        if (transfert.getCorridor() != null) {
            if (transfert.getCorridor().getPaysOrigine() != null && transfert.getCorridor().getPaysDestination() != null) {
                corridorDescription = transfert.getCorridor().getPaysOrigine().getNom()
                        + " \u2192 " + transfert.getCorridor().getPaysDestination().getNom();
            }
            if (transfert.getCorridor().getDeviseSource() != null) {
                deviseSourceCode = transfert.getCorridor().getDeviseSource().getCode();
            }
            if (transfert.getCorridor().getDeviseDestination() != null) {
                deviseDestinationCode = transfert.getCorridor().getDeviseDestination().getCode();
            }
            tauxChange = transfert.getCorridor().getTauxChange();
        }

        String expediteurNomComplet = null;
        String expediteurTelephone = null;
        String expediteurEmail = null;
        String expediteurPieceIdentite = null;
        String expediteurPays = null;
        if (transfert.getExpediteur() != null) {
            expediteurNomComplet = transfert.getExpediteur().getPrenom() + " " + transfert.getExpediteur().getNom();
            expediteurTelephone = transfert.getExpediteur().getTelephone();
            expediteurEmail = transfert.getExpediteur().getEmail();
            expediteurPieceIdentite = transfert.getExpediteur().getNumPieceIdentite();
            if (transfert.getExpediteur().getPays() != null) {
                expediteurPays = transfert.getExpediteur().getPays().getNom();
            }
        }

        String beneficiaireNomComplet = null;
        String beneficiaireTelephone = null;
        String beneficiairePays = null;
        if (transfert.getBeneficiaire() != null) {
            beneficiaireNomComplet = transfert.getBeneficiaire().getPrenom() + " " + transfert.getBeneficiaire().getNom();
            beneficiaireTelephone = transfert.getBeneficiaire().getTelephone();
            if (transfert.getBeneficiaire().getPays() != null) {
                beneficiairePays = transfert.getBeneficiaire().getPays().getNom();
            }
        }

        BigDecimal totalFrais = fraisFixe != null && fraisVariable != null
                ? fraisFixe.add(fraisVariable)
                : transfert.getFrais();

        BigDecimal montantNet = transfert.getMontantNet();

        String tauxApplique = null;
        if (tauxChange != null && deviseSourceCode != null && deviseDestinationCode != null) {
            tauxApplique = "1 " + deviseSourceCode + " = " + tauxChange.setScale(3, RoundingMode.HALF_UP) + " " + deviseDestinationCode;
        }

        return TransfertMobileResponseDTO.builder()
                .id(transfert.getId())
                .reference(transfert.getCodeRetrait())
                .corridorDescription(corridorDescription)
                .dateEnvoi(transfert.getDateCreation())
                .operateur(mobile != null && mobile.getOperateur() != null ? mobile.getOperateur().name() : null)
                .statut(transfert.getStatut() != null ? transfert.getStatut().name() : null)
                .expediteurNomComplet(expediteurNomComplet)
                .expediteurTelephone(expediteurTelephone)
                .expediteurEmail(expediteurEmail)
                .expediteurPieceIdentite(expediteurPieceIdentite)
                .expediteurPays(expediteurPays)
                .beneficiaireNomComplet(beneficiaireNomComplet)
                .beneficiaireTelephone(beneficiaireTelephone)
                .beneficiairePays(beneficiairePays)
                .montantDepart(transfert.getMontantEnvoye())
                .fraisFixes(fraisFixe)
                .fraisProportionnels(fraisVariable)
                .totalFrais(totalFrais)
                .montantNetApresFrais(montantNet)
                .tauxApplique(tauxApplique)
                .sourceTaux("Banque Centrale / March\u00e9")
                .montantRecu(montantNet)
                .deviseSource(deviseSourceCode)
                .deviseDestination(deviseDestinationCode)
                .build();
    }
}
