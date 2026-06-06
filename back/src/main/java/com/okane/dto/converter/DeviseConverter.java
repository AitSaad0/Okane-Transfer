package com.okane.dto.converter;

import org.springframework.stereotype.Component;
import com.okane.entity.Devise;
import com.okane.dto.requestDto.DeviseRequestDTO;
import com.okane.dto.responseDto.DeviseResponseDTO;

@Component
public class DeviseConverter {
    public Devise toEntity(DeviseRequestDTO dto) {
        return Devise.builder()
                .code(dto.getCode())
                .symbole(dto.getSymbole())
                .nom(dto.getNom())
                .build();
    }

    public DeviseResponseDTO toDTO(Devise devise) {
        return DeviseResponseDTO.builder()
                .id(devise.getId())
                .code(devise.getCode())
                .symbole(devise.getSymbole())
                .nom(devise.getNom())
                .build();
    }
}
