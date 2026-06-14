package com.okane.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class EcartCaisseRequestDTO {

    @NotNull(message = "Le montant de l'écart est obligatoire")
    private BigDecimal montantEcart;

    @NotBlank(message = "Le motif est obligatoire")
    private String motif;

    public EcartCaisseRequestDTO() {}

    public BigDecimal getMontantEcart() { return montantEcart; }
    public void setMontantEcart(BigDecimal montantEcart) { this.montantEcart = montantEcart; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
}