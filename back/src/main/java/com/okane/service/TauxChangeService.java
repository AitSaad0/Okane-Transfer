package com.okane.service;

import com.okane.dto.requestDto.TauxChangeRequestDTO;
import com.okane.dto.responseDto.ConversionResponseDTO;
import com.okane.dto.responseDto.TauxChangeResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface TauxChangeService {
    List<TauxChangeResponseDTO> findAllCurrentRates();
    TauxChangeResponseDTO updateManual(Long corridorId, TauxChangeRequestDTO dto);
    void syncFromExternalApi();
    List<TauxChangeResponseDTO> getHistory(Long corridorId);
    ConversionResponseDTO convert(String from, String to, BigDecimal amount);
}