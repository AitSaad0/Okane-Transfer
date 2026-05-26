package com.okane.geographic_monetary_reference.service.facade;

import com.okane.geographic_monetary_reference.controller.dto.requestDto.PaysRequestDTO;
import com.okane.geographic_monetary_reference.controller.dto.responseDto.PaysResponseDTO;

import java.util.List;

public interface PaysService {
    List<PaysResponseDTO> findAll();
    PaysResponseDTO findById(Long id);
    PaysResponseDTO save(PaysRequestDTO dto);
    PaysResponseDTO update(Long id, PaysRequestDTO dto);
    void delete(Long id);
}
