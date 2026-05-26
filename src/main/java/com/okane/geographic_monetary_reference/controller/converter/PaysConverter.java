package com.okane.geographic_monetary_reference.controller.converter;

import com.okane.geographic_monetary_reference.bean.Pays;
import com.okane.geographic_monetary_reference.controller.dto.responseDto.PaysResponseDTO;
import com.okane.geographic_monetary_reference.controller.dto.requestDto.PaysRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class PaysConverter {
    public Pays toEntity(PaysRequestDTO dto) {
        return Pays.builder()
                .codeIso(dto.getCodeIso())
                .nom(dto.getNom())
                .build();
    }

    public PaysResponseDTO toDTO(Pays pays) {
        return PaysResponseDTO.builder()
                .id(pays.getId())
                .codeIso(pays.getCodeIso())
                .nom(pays.getNom())
                .build();
    }
}
