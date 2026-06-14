package com.okane.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ForceCancelRequestDTO {

    @NotBlank(message = "Le motif d'annulation est obligatoire")
    @Size(min = 10, max = 500, message = "Le motif doit contenir entre 10 et 500 caractères")
    private String motif;

    public ForceCancelRequestDTO() {}

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
}