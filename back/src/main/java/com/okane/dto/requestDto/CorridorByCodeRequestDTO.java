package com.okane.dto.requestDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorridorByCodeRequestDTO {
    private String paysSourceCode;
    private String paysDestinationCode;
    private String deviseSourceCode;
    private String deviseDestinationCode;
}