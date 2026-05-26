package com.okane.geographic_monetary_reference.controller.dto.responseDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaysResponseDTO {
    private Long id;
    private String codeIso;
    private String nom;
}
