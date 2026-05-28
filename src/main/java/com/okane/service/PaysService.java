package com.okane.service;

import com.okane.dto.requestDto.PaysRequestDTO;
import com.okane.dto.responseDto.PaysResponseDTO;

import java.util.List;

public interface PaysService {
    List<PaysResponseDTO> findAll();
    PaysResponseDTO findById(Long id);
    PaysResponseDTO save(PaysRequestDTO dto);
    PaysResponseDTO update(Long id, PaysRequestDTO dto);
    void delete(Long id);
}
