package com.okane.service.impl;


import com.okane.dto.responseDto.BroadcastNotificationResponse;
import com.okane.entity.Transfert;
import com.okane.entity.Notification;
import com.okane.dto.NotificationPreferenceDto;
import com.okane.dto.requestDto.BroadcastNotificationRequest;
import com.okane.dto.responseDto.NotificationResponseDto;
import com.okane.entity.User;
import com.okane.repository.UserRepository;
import com.okane.entity.NotificationPreference;
import com.okane.repository.NotificationPreferenceRepository;
import com.okane.repository.NotificationRepository;
import com.okane.service.EmailService;
import com.okane.service.NotificationService;
import com.okane.entity.enums.CanalNotification;
import com.okane.entity.enums.TypeNotification;
import com.okane.service.SmsService;
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
    private final SmsService smsService;

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
        if (canal == CanalNotification.SMS
                && user.getTelephone() != null) {

            try {
                smsService.sendSms(
                        user.getTelephone(),
                        contenu
                );
            } catch (Exception e) {
                // log and continue
            }
        }
        if (canal == CanalNotification.EMAIL
                && user.getEmail() != null) {
            try {
                emailService.send(
                        user.getEmail(),
                        buildSubject(type),
                        contenu
                );
            } catch (Exception e) {
                // email unavailable in dev, skip silently
            }
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
    @Override
    public List<BroadcastNotificationResponse> getAllBroadcasts() {
        List<TypeNotification> broadcastTypes = List.of(
                TypeNotification.SYSTEME,
                TypeNotification.MAINTENANCE,
                TypeNotification.ALERTE
        );
        return notificationRepository.findAllBroadcasts(broadcastTypes);
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