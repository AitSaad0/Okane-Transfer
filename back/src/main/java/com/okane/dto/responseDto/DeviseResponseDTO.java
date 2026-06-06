package com.okane.dto.responseDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviseResponseDTO {
    private Long id;
    private String code;
    private String symbole;
    private String nom;
}
