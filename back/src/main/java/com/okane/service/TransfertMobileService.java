package com.okane.service;

import com.okane.dto.requestDto.TransfertMobileRequestDTO;
import com.okane.dto.responseDto.TransfertMobileResponseDTO;

public interface TransfertMobileService {
    TransfertMobileResponseDTO creerTransfertMobile(TransfertMobileRequestDTO request, String agentEmail);
}
