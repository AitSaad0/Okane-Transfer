package com.okane.dto.responseDto;

import lombok.*;
import java.util.List;

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
    private boolean active;
    private List<String> countries;
}
