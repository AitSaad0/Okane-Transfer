package com.okane.dto.requestDto;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviseRequestDTO {
    private String code;
    private String symbole;
    private String nom;
    private List<String> countries;
}
