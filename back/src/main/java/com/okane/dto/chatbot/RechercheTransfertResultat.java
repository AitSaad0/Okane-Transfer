package com.okane.dto.chatbot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RechercheTransfertResultat(
        String reference,
        String typeTransfert,
        String statut,
        BigDecimal montant,
        String devise,
        String beneficiaireNom,
        String expediteurNom,
        String codeRetrait,
        LocalDateTime dateCreation
) {}
