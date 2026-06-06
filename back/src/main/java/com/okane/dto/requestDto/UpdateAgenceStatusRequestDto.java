package com.okane.dto.requestDto;

import com.okane.entity.enums.StatutAgence;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateAgenceStatusRequestDto {

    @NotNull(message = "Le statut est obligatoire")
    private StatutAgence statut;
}