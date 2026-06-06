package com.okane.service;

import com.okane.dto.requestDto.GrilleTarifaireRequestDTO;
import com.okane.dto.requestDto.SimulationRequestDTO;
import com.okane.dto.responseDto.GrilleTarifaireResponseDTO;
import com.okane.dto.responseDto.SimulationResponseDTO;

import java.util.List;

public interface GrilleTarifaireService {
    List<GrilleTarifaireResponseDTO> findAll();
    GrilleTarifaireResponseDTO findById(Long id);
    List<GrilleTarifaireResponseDTO> findByCorridorId(Long corridorId);
    GrilleTarifaireResponseDTO save(GrilleTarifaireRequestDTO dto);
    GrilleTarifaireResponseDTO update(Long id, GrilleTarifaireRequestDTO dto);
    void delete(Long id);
    SimulationResponseDTO simulate(SimulationRequestDTO dto);
    byte[] exportCsv();
    byte[] exportPdf();
}