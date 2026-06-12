package com.okane.dto.responseDto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransfertResponseDTO {
    private Long id;
    private String codeRetrait;
    private BigDecimal montantEnvoye;
    private BigDecimal frais;
    private BigDecimal montantNet;
    private String statut;

    private String expediteurNom;
    private String expediteurPrenom;
    private String expediteurTelephone;

    private String beneficiaireNom;
    private String beneficiairePrenom;
    private String beneficiaireTelephone;

    private String corridorDescription;
    private String deviseSource;
    private String deviseDestination;

    private String agentNom;
    private String agentPrenom;

    private LocalDateTime dateCreation;
}
