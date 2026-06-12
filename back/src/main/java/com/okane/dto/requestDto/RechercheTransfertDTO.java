package com.okane.dto.requestDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechercheTransfertDTO {
    private String codeRetrait;
    private String telephoneBeneficiaire;
}
