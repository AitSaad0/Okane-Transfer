package com.okane.security_system.service.impl;


import com.okane.clients_transfers.bean.Transfert;
import com.okane.network_users.bean.User;
import com.okane.network_users.repository.UserRepository;
import com.okane.security_system.bean.Notification;
import com.okane.security_system.bean.NotificationPreference;
import com.okane.security_system.controller.dto.*;
import com.okane.security_system.repository.NotificationPreferenceRepository;
import com.okane.security_system.repository.NotificationRepository;
import com.okane.security_system.service.facade.EmailService;
import com.okane.security_system.service.facade.NotificationService;
import com.okane.shared.CanalNotification;
import com.okane.shared.TypeNotification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;

    private final NotificationPreferenceRepository preferenceRepository;

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public List<NotificationResponseDto> getUserNotifications(User user) {

        return notificationRepository
                .findByUtilisateurOrderByDateEnvoiDesc(user)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void markAsRead(Long id) {

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow();

        notification.setLu(true);

        notificationRepository.save(notification);
    }

    @Override
    public void updatePreferences(
            User user,
            NotificationPreferenceDto dto
    ) {

        NotificationPreference prefs =
                preferenceRepository.findByUser(user)
                        .orElse(
                                NotificationPreference.builder()
                                        .user(user)
                                        .build()
                        );

        prefs.setEmailActive(dto.getEmailActive());
        prefs.setPushActive(dto.getPushActive());
        prefs.setSmsActive(dto.getSmsActive());

        preferenceRepository.save(prefs);
    }

    @Override
    public void sendTransferNotification(
            User user,
            Transfert transfert,
            TypeNotification type,
            CanalNotification canal,
            String contenu
    ) {

        Notification notification =
                Notification.builder()
                        .type(type)
                        .canal(canal)
                        .contenu(contenu)
                        .dateEnvoi(LocalDateTime.now())
                        .lu(false)
                        .utilisateur(user)
                        .transfert(transfert)
                        .build();

        notificationRepository.save(notification);

        if (canal == CanalNotification.EMAIL
                && user.getEmail() != null) {

            emailService.send(
                    user.getEmail(),
                    buildSubject(type),
                    contenu
            );
        }

        // PUSH
        // SMS
    }
    private String buildSubject(TypeNotification type) {

        return switch (type) {

            case TRANSFERT_CREE ->
                    "Transfert créé";

            case TRANSFERT_PAYE ->
                    "Transfert payé";

            case TRANSFERT_ANNULE ->
                    "Transfert annulé";

            case TRANSFERT_EXPIRE ->
                    "Transfert expiré";

            case RECU_EXPEDITEUR ->
                    "Reçu de transfert";

            case CONFIRMATION_RETRAIT ->
                    "Confirmation de retrait";

            case MAINTENANCE ->
                    "Maintenance système";

            case ALERTE ->
                    "Alerte sécurité";

            default ->
                    "Notification Okane";
        };
    }

    @Override
    public void broadcast(BroadcastNotificationRequest request) {

        List<User> users = userRepository.findAll();

        for (User user : users) {

            Notification notification =
                    Notification.builder()
                            .type(request.getType())
                            .canal(request.getCanal())
                            .contenu(request.getContenu())
                            .dateEnvoi(LocalDateTime.now())
                            .lu(false)
                            .utilisateur(user)
                            .build();

            notificationRepository.save(notification);
            if (request.getCanal() == CanalNotification.EMAIL
                    && user.getEmail() != null) {

                emailService.send(
                        user.getEmail(),
                        "Annonce système Okane",
                        request.getContenu()
                );
            }
        }
    }

    private NotificationResponseDto mapToDto(Notification n) {

        return NotificationResponseDto.builder()
                .id(n.getId())
                .type(n.getType())
                .canal(n.getCanal())
                .contenu(n.getContenu())
                .lu(n.getLu())
                .dateEnvoi(n.getDateEnvoi())
                .build();
    }
}