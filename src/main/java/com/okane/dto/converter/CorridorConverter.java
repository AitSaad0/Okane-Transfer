package com.okane.dto.converter;

import com.okane.dto.requestDto.CorridorRequestDTO;
import com.okane.dto.responseDto.CorridorResponseDTO;
import com.okane.entity.Corridor;
import com.okane.entity.Devise;
import com.okane.entity.Pays;
import org.springframework.stereotype.Component;

@Component
public class CorridorConverter {

    public CorridorResponseDTO toResponseDTO(Corridor corridor) {
        if (corridor == null) return null;

        Pays po = corridor.getPaysOrigine();
        Pays pd = corridor.getPaysDestination();
        Devise ds = corridor.getDeviseSource();
        Devise dd = corridor.getDeviseDestination();

        return CorridorResponseDTO.builder()
                .id(corridor.getId())
                .tauxChange(corridor.getTauxChange())
                .actif(corridor.getActif())
                .paysOrigineId(po != null ? po.getId() : null)
                .paysOrigineNom(po != null ? po.getNom() : null)
                .paysDestinationId(pd != null ? pd.getId() : null)
                .paysDestinationNom(pd != null ? pd.getNom() : null)
                .deviseSourceId(ds != null ? ds.getId() : null)
                .deviseSourceCode(ds != null ? ds.getCode() : null)
                .deviseSourceSymbole(ds != null ? ds.getSymbole() : null)
                .deviseDestinationId(dd != null ? dd.getId() : null)
                .deviseDestinationCode(dd != null ? dd.getCode() : null)
                .deviseDestinationSymbole(dd != null ? dd.getSymbole() : null)
                .build();
    }

    public Corridor toEntity(CorridorRequestDTO dto, Pays paysOrigine, Pays paysDestination,
                             Devise deviseSource, Devise deviseDestination) {
        if (dto == null) return null;

        return Corridor.builder()
                .paysOrigine(paysOrigine)
                .paysDestination(paysDestination)
                .deviseSource(deviseSource)
                .deviseDestination(deviseDestination)
                .tauxChange(dto.getTauxChange())
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .build();
    }

    public void updateEntityFromDTO(Corridor corridor, CorridorRequestDTO dto,
                                    Pays paysOrigine, Pays paysDestination,
                                    Devise deviseSource, Devise deviseDestination) {
        if (dto == null || corridor == null) return;

        if (paysOrigine != null) corridor.setPaysOrigine(paysOrigine);
        if (paysDestination != null) corridor.setPaysDestination(paysDestination);
        if (deviseSource != null) corridor.setDeviseSource(deviseSource);
        if (deviseDestination != null) corridor.setDeviseDestination(deviseDestination);
        if (dto.getTauxChange() != null) corridor.setTauxChange(dto.getTauxChange());
        if (dto.getActif() != null) corridor.setActif(dto.getActif());
    }
}