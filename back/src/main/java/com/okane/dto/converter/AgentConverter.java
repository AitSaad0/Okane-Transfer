package com.okane.dto.converter;

import com.okane.entity.Caisse;
import com.okane.entity.User;
import com.okane.dto.responseDto.AgentDetailResponseDto;
import com.okane.entity.enums.StatutCaisse;
import org.springframework.stereotype.Component;

@Component
public class AgentConverter {

        public AgentDetailResponseDto toDto(User user) {

            // caisse OUVERTE de l'agent si elle existe
            Caisse caisseActive = user.getCaisses() != null
                    ? user.getCaisses().stream()
                    .filter(c -> c.getStatut() == StatutCaisse.OUVERTE)
                    .findFirst()
                    .orElse(null)
                    : null;

            return AgentDetailResponseDto.builder()
                    .id(user.getId())
                    .nom(user.getNom())
                    .prenom(user.getPrenom())
                    .email(user.getEmail())
                    .telephone(user.getTelephone())
                    .role(user.getRole())
                    .active(user.getActive())
                    .agenceId(user.getAgence() != null ? user.getAgence().getId()   : null)
                    .agenceNom(user.getAgence() != null ? user.getAgence().getNom() : null)
                    .agenceVille(user.getAgence() != null ? user.getAgence().getVille() : null)
                    .caisseId(caisseActive != null ? caisseActive.getId().toString() : null)
                    .caisseOuverte(caisseActive != null)
                    .soldeCaisse(caisseActive != null ? caisseActive.getSoldeCourant() : null)
                    .dateOuvertureCaisse(caisseActive != null ? caisseActive.getDateOuverture() : null)
                    .build();
        }

}
