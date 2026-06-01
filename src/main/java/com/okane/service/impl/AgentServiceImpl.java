package com.okane.service.impl;

import com.okane.entity.Agence;
import com.okane.entity.User;
import com.okane.dto.converter.AgentConverter;
import com.okane.dto.requestDto.AssignAgentRequestDto;
import com.okane.dto.requestDto.UpdateAgentStatusRequestDto;
import com.okane.dto.responseDto.AgentDetailResponseDto;
import com.okane.repository.AgenceRepository;
import com.okane.repository.UserRepository;
import com.okane.service.AgentService;
import com.okane.entity.enums.Role;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final UserRepository   userRepository;
    private final AgenceRepository agenceRepository;
    private final AgentConverter   agentConverter;

    @Override
    @Transactional(readOnly = true)
    public List<AgentDetailResponseDto> getAgentsByAgence(Long agenceId) {
        // vérifier que l'agence existe
        if (!agenceRepository.existsById(agenceId)) {
            throw new EntityNotFoundException("Agence introuvable avec l'id : " + agenceId);
        }

        return userRepository
                .findByAgenceIdAndRoleAndDeletedFalse(agenceId, Role.AGENT)
                .stream()
                .map(agentConverter::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AgentDetailResponseDto assignAgent(Long agenceId, AssignAgentRequestDto request) {
        Agence agence = agenceRepository.findById(agenceId)
                .orElseThrow(() -> new EntityNotFoundException("Agence introuvable avec l'id : " + agenceId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable avec l'id : " + request.getUserId()));

        // vérifications métier
        if (user.getRole() != Role.AGENT) {
            throw new IllegalArgumentException("L'utilisateur doit avoir le rôle AGENT");
        }
        if (user.getDeleted()) {
            throw new IllegalArgumentException("Impossible d'affecter un utilisateur supprimé");
        }
        if (user.getAgence() != null && user.getAgence().getId().equals(agenceId)) {
            throw new IllegalArgumentException("Cet agent est déjà affecté à cette agence");
        }

        user.setAgence(agence);
        return agentConverter.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void removeAgent(Long agenceId, Long userId) {
        if (!agenceRepository.existsById(agenceId)) {
            throw new EntityNotFoundException("Agence introuvable avec l'id : " + agenceId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable avec l'id : " + userId));

        if (!userRepository.existsByIdAndAgenceId(userId, agenceId)) {
            throw new IllegalArgumentException("Cet agent n'appartient pas à cette agence");
        }

        user.setAgence(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public AgentDetailResponseDto updateAgentStatus(Long agenceId, Long userId,
                                                    UpdateAgentStatusRequestDto request) {
        if (!agenceRepository.existsById(agenceId)) {
            throw new EntityNotFoundException("Agence introuvable avec l'id : " + agenceId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable avec l'id : " + userId));

        if (!userRepository.existsByIdAndAgenceId(userId, agenceId)) {
            throw new IllegalArgumentException("Cet agent n'appartient pas à cette agence");
        }

        user.setActive(request.getActive());
        return agentConverter.toDto(userRepository.save(user));
    }
}