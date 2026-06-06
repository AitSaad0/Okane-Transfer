package com.okane.service;

import com.okane.dto.responseDto.DeviseResponseDTO;
import com.okane.dto.requestDto.DeviseRequestDTO;

import java.util.List;

public interface DeviseService {
    List<DeviseResponseDTO> findAll();
    DeviseResponseDTO findById(Long id);
    DeviseResponseDTO save(DeviseRequestDTO dto);
    DeviseResponseDTO update(Long id, DeviseRequestDTO dto);
    void delete(Long id);
}