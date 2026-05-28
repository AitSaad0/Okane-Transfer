package com.okane.network_users.service.facade;

import com.okane.network_users.controller.dto.requestDto.AssignAgentRequestDto;
import com.okane.network_users.controller.dto.requestDto.UpdateAgentStatusRequestDto;
import com.okane.network_users.controller.dto.responseDto.AgentDetailResponseDto;

import java.util.List;

public interface AgentService {

    List<AgentDetailResponseDto> getAgentsByAgence(Long agenceId);

    AgentDetailResponseDto assignAgent(Long agenceId, AssignAgentRequestDto request);

    void removeAgent(Long agenceId, Long userId);

    AgentDetailResponseDto updateAgentStatus(Long agenceId, Long userId, UpdateAgentStatusRequestDto request);
}