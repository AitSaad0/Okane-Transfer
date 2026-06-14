package com.okane.service;

import com.okane.dto.responseDto.ManagerDashboardResponseDTO;

public interface ManagerDashboardService {

    // KPIs temps réel de l'agence
    ManagerDashboardResponseDTO getDashboard(String managerEmail);

    // Rapport journalier (données pour export ou affichage)
    ManagerDashboardResponseDTO getRapportJournalier(String managerEmail, String date);

    // Export PDF ou CSV (retourne les bytes)
    byte[] exportRapport(String managerEmail, String format, String date);
}