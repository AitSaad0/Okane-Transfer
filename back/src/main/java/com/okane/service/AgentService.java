package com.okane.service;

import com.okane.dto.requestDto.AssignAgentRequestDto;
import com.okane.dto.requestDto.UpdateAgentStatusRequestDto;
import com.okane.dto.responseDto.AgentDetailResponseDto;

import java.util.List;

public interface AgentService {

    List<AgentDetailResponseDto> getAgentsByAgence(Long agenceId);

    AgentDetailResponseDto assignAgent(Long agenceId, AssignAgentRequestDto request);

    void removeAgent(Long agenceId, Long userId);

    AgentDetailResponseDto updateAgentStatus(Long agenceId, Long userId, UpdateAgentStatusRequestDto request);
}