package com.okane.dto.chatbot;

public record RechercheCorridorResultat(
        Long id,
        String paysSource,
        String paysDestination,
        String deviseSource,
        String deviseDestination,
        boolean actif
) {}
