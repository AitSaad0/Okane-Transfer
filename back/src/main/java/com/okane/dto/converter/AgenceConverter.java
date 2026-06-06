package com.okane.dto.converter;
import com.okane.entity.Agence;
import com.okane.dto.responseDto.AgenceResponseDto;
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
