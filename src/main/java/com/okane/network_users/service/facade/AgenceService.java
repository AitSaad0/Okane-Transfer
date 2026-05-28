package com.okane.network_users.service.facade;

import com.okane.network_users.controller.dto.requestDto.CreateAgenceRequestDto;
import com.okane.network_users.controller.dto.requestDto.UpdateAgenceRequestDto;
import com.okane.network_users.controller.dto.requestDto.UpdateAgenceStatusRequestDto;
import com.okane.network_users.controller.dto.responseDto.AgenceDashboardResponseDto;
import com.okane.network_users.controller.dto.responseDto.AgenceResponseDto;
import com.okane.pagination.PageResponseDto;
import com.okane.shared.StatutAgence;

public interface AgenceService {

    PageResponseDto<AgenceResponseDto> getAllAgences(Long paysId, StatutAgence statut, int page, int size, String sort);

    AgenceResponseDto getAgenceById(Long id);

    AgenceResponseDto createAgence(CreateAgenceRequestDto request);

    AgenceResponseDto updateAgence(Long id, UpdateAgenceRequestDto request);

    AgenceResponseDto updateAgenceStatus(Long id, UpdateAgenceStatusRequestDto request);

    AgenceDashboardResponseDto getAgenceDashboard(Long id);
}