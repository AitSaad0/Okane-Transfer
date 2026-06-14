package com.okane.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class ClotureCaisseRequestDTO {

    // Solde physiquement compté par l'agent
    @NotNull(message = "Le solde compté est obligatoire")
    @PositiveOrZero(message = "Le solde ne peut pas être négatif")
    private BigDecimal soldeCompte;

    private String observation;

    public ClotureCaisseRequestDTO() {}

    public BigDecimal getSoldeCompte() { return soldeCompte; }
    public void setSoldeCompte(BigDecimal soldeCompte) { this.soldeCompte = soldeCompte; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public void setSoldePhysique(BigDecimal bigDecimal) {
    }

    public void setCommentaire(String clôtureFinDeJournée) {
    }
}