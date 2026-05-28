// ClientProfileServiceImpl.java
package com.okane.clients_transfers.service.impl;

import com.okane.clients_transfers.bean.Client;
import com.okane.clients_transfers.controller.converter.ClientConverter;
import com.okane.clients_transfers.controller.dto.requestDto.UpdateClientProfileRequestDto;
import com.okane.clients_transfers.controller.dto.responseDto.ClientActivityResponseDto;
import com.okane.clients_transfers.controller.dto.responseDto.ClientProfileResponseDto;
import com.okane.pagination.PageResponseDto;
import com.okane.clients_transfers.repository.ClientRepository;
import com.okane.security_system.repository.JournalAuditRepository;
import com.okane.clients_transfers.service.facade.ClientProfileService;
import com.okane.network_users.bean.User;
import com.okane.network_users.repository.UserRepository;
import com.okane.security_system.bean.JournalAudit;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientProfileServiceImpl implements ClientProfileService {

    private final ClientRepository      clientRepository;
    private final ClientConverter       clientConverter;
    private final UserRepository        userRepository;
    private final JournalAuditRepository journalAuditRepository;

    @Override
    @Transactional(readOnly = true)
    public ClientProfileResponseDto getMyProfile(Long userId) {
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profil client introuvable"));
        return clientConverter.toProfileDto(client);
    }

    @Override
    @Transactional
    public ClientProfileResponseDto updateMyProfile(Long userId, UpdateClientProfileRequestDto request) {
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profil client introuvable"));

        // vérif téléphone unique si changé
        if (!client.getTelephone().equals(request.getTelephone())
                && clientRepository.existsByTelephone(request.getTelephone())) {
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé");
        }

        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setTelephone(request.getTelephone());
        clientRepository.save(client);

        // préférences de notification sur User
        User user = client.getUser();
        if (user != null) {
            if (request.getNotificationEmail() != null)
                user.setNotificationEmail(request.getNotificationEmail());
            if (request.getNotificationSms() != null)
                user.setNotificationSms(request.getNotificationSms());
            if (request.getNotificationPush() != null)
                user.setNotificationPush(request.getNotificationPush());
            userRepository.save(user);
        }

        return clientConverter.toProfileDto(client);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ClientActivityResponseDto> getMyActivity(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<JournalAudit> auditPage = journalAuditRepository
                .findByUtilisateurId(userId, pageable);

        List<ClientActivityResponseDto> content = auditPage.getContent()
                .stream()
                .map(a -> ClientActivityResponseDto.builder()
                        .action(a.getAction())
                        .details(a.getDetails())
                        .ipAddress(a.getIpAddress())
                        .timestamp(a.getTimestamp())
                        .type(a.getType())
                        .build())
                .toList();

        return new PageResponseDto<>(
                content,
                auditPage.getNumber(),
                auditPage.getSize(),
                auditPage.getTotalElements(),
                auditPage.getTotalPages(),
                auditPage.isLast()
        );
    }
}