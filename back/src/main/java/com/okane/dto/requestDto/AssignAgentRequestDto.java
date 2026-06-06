package com.okane.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssignAgentRequestDto {

    @NotNull(message = "L'id de l'utilisateur est obligatoire")
    private Long userId;
}