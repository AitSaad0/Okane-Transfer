package com.okane.dto.chatbot;

import java.math.BigDecimal;

public record RechercheGrilleTarifaireResultat(
        Long corridorId,
        BigDecimal montantMin,
        BigDecimal montantMax,
        BigDecimal fraisFixe,
        BigDecimal fraisProportionnel,
        String devise
) {}
