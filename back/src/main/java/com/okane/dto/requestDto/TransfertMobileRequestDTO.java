package com.okane.dto.requestDto;

import com.okane.entity.enums.OperateurMobile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransfertMobileRequestDTO {

    @NotNull
    private OperateurMobile operateur;

    @NotNull
    private Long corridorId;

    @Valid
    @NotNull
    private InfoPersonne expediteur;

    @Valid
    @NotNull
    private InfoPersonne beneficiaire;

    @NotNull
    @Positive
    private BigDecimal montantEnvoye;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InfoPersonne {
        @NotBlank
        private String nom;

        @NotBlank
        private String prenom;

        @NotBlank
        private String telephone;

        @NotNull
        private Long paysId;

        private String numPieceIdentite;

        private String email;
    }
}
