package com.okane.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementResponseDTO {

    private Long id;
    private String codeRetrait;
    private String reference;
    private String corridorDescription;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateEnvoi;
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
    private BigDecimal montantNet;

    private BigDecimal tauxChange;
    private String sourceTaux;

    private BigDecimal montantRecu;
    private String deviseSource;
    private String deviseDestination;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime datePaiement;

    private String agentEnvoiNom;
    private String agentEnvoiPrenom;
    private String agentPaiementNom;
    private String agentPaiementPrenom;

    private boolean paye;
}
