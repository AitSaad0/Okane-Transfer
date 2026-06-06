package com.okane.service;

import com.okane.dto.requestDto.CorridorRequestDTO;
import com.okane.dto.responseDto.CorridorResponseDTO;

import java.util.List;

public interface CorridorService {
    List<CorridorResponseDTO> findAll();
    List<CorridorResponseDTO> findActive();
    CorridorResponseDTO findById(Long id);
    CorridorResponseDTO save(CorridorRequestDTO dto);
    CorridorResponseDTO update(Long id, CorridorRequestDTO dto);
    void delete(Long id);
    void toggleStatus(Long id);
}