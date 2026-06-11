package com.okane.service;

import com.okane.dto.requestDto.PaiementRequestDTO;
import com.okane.dto.responseDto.PaiementResponseDTO;

import java.util.List;

public interface PaiementService {
    PaiementResponseDTO rechercherParCodeRetrait(String codeRetrait);
    List<PaiementResponseDTO> rechercherParTelephoneBeneficiaire(String telephone);
    PaiementResponseDTO payerTransfert(PaiementRequestDTO request, String agentEmail);
}
