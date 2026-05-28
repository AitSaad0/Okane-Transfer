package com.okane.network_users.controller.dto.responseDto;

import com.okane.shared.StatutAgence;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AgenceResponseDto {

    private Long        id;
    private String      nom;
    private String      adresse;
    private String      ville;
    private String      codePostal;
    private BigDecimal  plafondJournalier;
    private StatutAgence statut;
    private String      paysNom;
    private String      paysCode;
}