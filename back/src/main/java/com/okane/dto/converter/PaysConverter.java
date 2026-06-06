package com.okane.dto.converter;

import com.okane.entity.Pays;
import com.okane.dto.responseDto.PaysResponseDTO;
import com.okane.dto.requestDto.PaysRequestDTO;
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
