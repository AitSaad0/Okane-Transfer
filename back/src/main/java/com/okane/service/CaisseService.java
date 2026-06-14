package com.okane.service;

import com.okane.dto.requestDto.ClotureCaisseRequestDTO;
import com.okane.dto.requestDto.EcartCaisseRequestDTO;
import com.okane.dto.responseDto.CaisseResponseDTO;

import java.util.List;

public interface CaisseService {

    // Caisse courante de l'agent (solde + statut)
    CaisseResponseDTO getCaisseCourante(String agentEmail);

    // Opérations du jour (liste détaillée)
    CaisseResponseDTO getOperationsDuJour(String agentEmail);

    // Clôture journalière avec réconciliation
    CaisseResponseDTO cloturerCaisse(String agentEmail, ClotureCaisseRequestDTO request);

    // Signalement d'un écart
    void signalerEcart(String agentEmail, EcartCaisseRequestDTO request);

    // Vue manager : toutes les caisses des agents de l'agence
    List<CaisseResponseDTO> getCaissesAgence(String managerEmail);
}