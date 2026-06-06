package com.okane.service;

import com.okane.dto.requestDto.CreateAgenceRequestDto;
import com.okane.dto.requestDto.UpdateAgenceRequestDto;
import com.okane.dto.requestDto.UpdateAgenceStatusRequestDto;
import com.okane.dto.responseDto.AgenceDashboardResponseDto;
import com.okane.dto.responseDto.AgenceResponseDto;
import com.okane.pagination.PageResponseDto;
import com.okane.entity.enums.StatutAgence;

public interface AgenceService {

    PageResponseDto<AgenceResponseDto> getAllAgences(Long paysId, StatutAgence statut, int page, int size, String sort);

    AgenceResponseDto getAgenceById(Long id);

    AgenceResponseDto createAgence(CreateAgenceRequestDto request);

    AgenceResponseDto updateAgence(Long id, UpdateAgenceRequestDto request);

    AgenceResponseDto updateAgenceStatus(Long id, UpdateAgenceStatusRequestDto request);

    AgenceDashboardResponseDto getAgenceDashboard(Long id);
}