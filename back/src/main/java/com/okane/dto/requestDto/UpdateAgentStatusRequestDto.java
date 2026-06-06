package com.okane.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateAgentStatusRequestDto {

    @NotNull(message = "Le statut est obligatoire")
    private Boolean active;  // true = réactiver, false = suspendre
}