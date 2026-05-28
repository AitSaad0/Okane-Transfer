package com.okane.network_users.controller.dto.requestDto;

import com.okane.shared.StatutAgence;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateAgenceStatusRequestDto {

    @NotNull(message = "Le statut est obligatoire")
    private StatutAgence statut;
}