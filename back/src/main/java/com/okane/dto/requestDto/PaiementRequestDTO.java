package com.okane.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementRequestDTO {

    @NotNull
    private Long transfertId;

    @NotBlank
    private String pieceIdentiteBeneficiaire;

    @NotBlank
    private String codeRetrait;
}
