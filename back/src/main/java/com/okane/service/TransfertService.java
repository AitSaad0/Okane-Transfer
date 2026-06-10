package com.okane.service;

import com.okane.dto.requestDto.TransfertRequestDTO;
import com.okane.dto.responseDto.TransfertResponseDTO;

public interface TransfertService {
    TransfertResponseDTO creerTransfert(TransfertRequestDTO request, String agentEmail);
}
