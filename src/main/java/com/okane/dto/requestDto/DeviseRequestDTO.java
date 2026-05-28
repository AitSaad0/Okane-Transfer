package com.okane.dto.requestDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviseRequestDTO {
    private String code;
    private String symbole;
    private String nom;
}
