package com.okane.geographic_monetary_reference.service.facade;

import com.okane.geographic_monetary_reference.controller.dto.responseDto.DeviseResponseDTO;
import com.okane.geographic_monetary_reference.controller.dto.requestDto.DeviseRequestDTO;

import java.util.List;

public interface DeviseService {
    List<DeviseResponseDTO> findAll();
    DeviseResponseDTO findById(Long id);
    DeviseResponseDTO save(DeviseRequestDTO dto);
    DeviseResponseDTO update(Long id, DeviseRequestDTO dto);
    void delete(Long id);
}