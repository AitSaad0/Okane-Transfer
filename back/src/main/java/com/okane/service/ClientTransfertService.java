package com.okane.service;

import com.okane.dto.requestDto.ForceCancelRequestDTO;
import com.okane.dto.responseDto.TransfertResponseDTO;
import com.okane.pagination.PageResponseDto;
import org.springframework.data.domain.Pageable;

public interface ClientTransfertService {

    // --- CLIENT : ses propres transferts ---
    PageResponseDto<TransfertResponseDTO> getTransfertsClient(String userEmail, Pageable pageable);
    TransfertResponseDTO getTransfertClientById(Long id, String userEmail);

    // Suivi public (sans auth) par code de retrait
    TransfertResponseDTO trackTransfert(String codeRetrait);

    // --- ADMIN : vue globale ---
    PageResponseDto<TransfertResponseDTO> getAllTransfertsAdmin(
            String statut, Long agenceId, Long corridorId,
            String debut, String fin, Pageable pageable);

    TransfertResponseDTO getTransfertAdminById(Long id);

    void forceCancelTransfert(Long id, ForceCancelRequestDTO request, String adminEmail);

    // --- MANAGER : vue agence ---
    PageResponseDto<TransfertResponseDTO> getTransfertsManager(
            String managerEmail, String statut, Pageable pageable);
}