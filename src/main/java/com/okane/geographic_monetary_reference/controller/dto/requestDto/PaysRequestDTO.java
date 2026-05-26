package com.okane.geographic_monetary_reference.controller.dto.requestDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaysRequestDTO {
    private String codeIso;
    private String nom;
}
