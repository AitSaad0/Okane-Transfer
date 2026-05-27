package com.okane.security_system.service.facade;


import com.okane.clients_transfers.bean.Transfert;
import com.okane.network_users.bean.User;
import com.okane.security_system.controller.dto.*;
import com.okane.shared.CanalNotification;
import com.okane.shared.TypeNotification;

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
}