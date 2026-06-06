package com.okane.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateAgenceRequestDto {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @NotBlank(message = "Le code postal est obligatoire")
    private String codePostal;

    @NotNull(message = "Le plafond journalier est obligatoire")
    @Positive(message = "Le plafond doit être positif")
    private BigDecimal plafondJournalier;

    @NotNull(message = "Le pays est obligatoire")
    private Long paysId;
}