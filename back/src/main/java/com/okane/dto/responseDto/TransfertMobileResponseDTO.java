package com.okane.dto.responseDto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransfertMobileResponseDTO {
    private Long id;
    private String reference;
    private String corridorDescription;
    private LocalDateTime dateEnvoi;
    private String operateur;
    private String statut;

    private String expediteurNomComplet;
    private String expediteurTelephone;
    private String expediteurEmail;
    private String expediteurPieceIdentite;
    private String expediteurPays;

    private String beneficiaireNomComplet;
    private String beneficiaireTelephone;
    private String beneficiairePays;

    private BigDecimal montantDepart;
    private BigDecimal fraisFixes;
    private BigDecimal fraisProportionnels;
    private BigDecimal totalFrais;
    private BigDecimal montantNetApresFrais;

    private String tauxApplique;
    private String sourceTaux;

    private BigDecimal montantRecu;
    private String deviseSource;
    private String deviseDestination;
}
