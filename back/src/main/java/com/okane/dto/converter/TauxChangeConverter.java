package com.okane.dto.converter;

import com.okane.dto.responseDto.TauxChangeResponseDTO;
import com.okane.entity.Corridor;
import com.okane.entity.TauxChangeHistorique;
import org.springframework.stereotype.Component;

@Component
public class TauxChangeConverter {

    public TauxChangeResponseDTO toCurrentRateDTO(Corridor corridor) {
        if (corridor == null) return null;

        return TauxChangeResponseDTO.builder()
                .id(corridor.getId())
                .taux(corridor.getTauxChange())
                .source("ACTUEL")
                .corridorId(corridor.getId())
                .paysOrigineNom(corridor.getPaysOrigine() != null ? corridor.getPaysOrigine().getNom() : null)
                .paysDestinationNom(corridor.getPaysDestination() != null ? corridor.getPaysDestination().getNom() : null)
                .deviseSourceCode(corridor.getDeviseSource() != null ? corridor.getDeviseSource().getCode() : null)
                .deviseDestinationCode(corridor.getDeviseDestination() != null ? corridor.getDeviseDestination().getCode() : null)
                .deviseSourceSymbole(corridor.getDeviseSource() != null ? corridor.getDeviseSource().getSymbole() : null)
                .deviseDestinationSymbole(corridor.getDeviseDestination() != null ? corridor.getDeviseDestination().getSymbole() : null)
                .build();
    }

    public TauxChangeResponseDTO toHistoryDTO(TauxChangeHistorique historique) {
        if (historique == null) return null;

        Corridor corridor = historique.getCorridor();

        return TauxChangeResponseDTO.builder()
                .id(historique.getId())
                .taux(historique.getTauxNouveau())
                .source(historique.getSource())
                .dateMiseAJour(historique.getDateChangement())
                .corridorId(corridor != null ? corridor.getId() : null)
                .paysOrigineNom(corridor != null && corridor.getPaysOrigine() != null ? corridor.getPaysOrigine().getNom() : null)
                .paysDestinationNom(corridor != null && corridor.getPaysDestination() != null ? corridor.getPaysDestination().getNom() : null)
                .deviseSourceCode(corridor != null && corridor.getDeviseSource() != null ? corridor.getDeviseSource().getCode() : null)
                .deviseDestinationCode(corridor != null && corridor.getDeviseDestination() != null ? corridor.getDeviseDestination().getCode() : null)
                .deviseSourceSymbole(corridor != null && corridor.getDeviseSource() != null ? corridor.getDeviseSource().getSymbole() : null)
                .deviseDestinationSymbole(corridor != null && corridor.getDeviseDestination() != null ? corridor.getDeviseDestination().getSymbole() : null)
                .build();
    }
}