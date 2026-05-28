// AgenceConverter.java
package com.okane.network_users.controller.converter;

import com.okane.network_users.bean.Agence;
import com.okane.network_users.controller.dto.responseDto.AgenceResponseDto;
import org.springframework.stereotype.Component;

@Component
public class AgenceConverter {

    public AgenceResponseDto toDto(Agence agence) {
        return AgenceResponseDto.builder()
                .id(agence.getId())
                .nom(agence.getNom())
                .adresse(agence.getAdresse())
                .ville(agence.getVille())
                .codePostal(agence.getCodePostal())
                .plafondJournalier(agence.getPlafondJournalier())
                .statut(agence.getStatut())
                .paysNom(agence.getPays() != null ? agence.getPays().getNom()     : null)
                .paysCode(agence.getPays() != null ? agence.getPays().getCodeIso() : null)
                .build();
    }
}