package com.okane.dto.converter;

import com.okane.dto.responseDto.TransfertResponseDTO;
import com.okane.entity.Transfert;
import org.springframework.stereotype.Component;

@Component
public class TransfertConverter {

    public TransfertResponseDTO toResponseDTO(Transfert transfert) {
        if (transfert == null) return null;

        String corridorDescription = null;
        String deviseSource = null;
        String deviseDestination = null;
        if (transfert.getCorridor() != null) {
            if (transfert.getCorridor().getPaysOrigine() != null && transfert.getCorridor().getPaysDestination() != null) {
                corridorDescription = transfert.getCorridor().getPaysOrigine().getNom()
                        + " → " + transfert.getCorridor().getPaysDestination().getNom();
            }
            if (transfert.getCorridor().getDeviseSource() != null) {
                deviseSource = transfert.getCorridor().getDeviseSource().getCode();
            }
            if (transfert.getCorridor().getDeviseDestination() != null) {
                deviseDestination = transfert.getCorridor().getDeviseDestination().getCode();
            }
        }

        String agentNom = null;
        String agentPrenom = null;
        if (transfert.getAgentEnvoi() != null) {
            agentNom = transfert.getAgentEnvoi().getNom();
            agentPrenom = transfert.getAgentEnvoi().getPrenom();
        }

        return TransfertResponseDTO.builder()
                .id(transfert.getId())
                .codeRetrait(transfert.getCodeRetrait())
                .montantEnvoye(transfert.getMontantEnvoye())
                .frais(transfert.getFrais())
                .montantNet(transfert.getMontantNet())
                .statut(transfert.getStatut().name())
                .expediteurNom(transfert.getExpediteur() != null ? transfert.getExpediteur().getNom() : null)
                .expediteurPrenom(transfert.getExpediteur() != null ? transfert.getExpediteur().getPrenom() : null)
                .expediteurTelephone(transfert.getExpediteur() != null ? transfert.getExpediteur().getTelephone() : null)
                .beneficiaireNom(transfert.getBeneficiaire() != null ? transfert.getBeneficiaire().getNom() : null)
                .beneficiairePrenom(transfert.getBeneficiaire() != null ? transfert.getBeneficiaire().getPrenom() : null)
                .beneficiaireTelephone(transfert.getBeneficiaire() != null ? transfert.getBeneficiaire().getTelephone() : null)
                .corridorDescription(corridorDescription)
                .deviseSource(deviseSource)
                .deviseDestination(deviseDestination)
                .agentNom(agentNom)
                .agentPrenom(agentPrenom)
                .dateCreation(transfert.getDateCreation())
                .build();
    }
}
