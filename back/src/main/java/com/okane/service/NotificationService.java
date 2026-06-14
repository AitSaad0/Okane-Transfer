package com.okane.service;


import com.okane.dto.responseDto.BroadcastNotificationResponse;
import com.okane.entity.Transfert;
import com.okane.dto.NotificationPreferenceDto;
import com.okane.dto.requestDto.BroadcastNotificationRequest;
import com.okane.dto.responseDto.NotificationResponseDto;
import com.okane.entity.User;
import com.okane.entity.enums.CanalNotification;
import com.okane.entity.enums.TypeNotification;

import java.util.List;

public interface NotificationService {

    List<NotificationResponseDto> getUserNotifications(User user);

    void markAsRead(Long id);

    void updatePreferences(
            User user,
            NotificationPreferenceDto dto
    );

    void sendTransferNotification(
            User user,
            Transfert transfert,
            TypeNotification type,
            CanalNotification canal,
            String contenu
    );

    void broadcast(BroadcastNotificationRequest request);
    List<BroadcastNotificationResponse> getAllBroadcasts();}