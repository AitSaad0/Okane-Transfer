package com.okane.repository;

import com.okane.dto.responseDto.BroadcastNotificationResponse;
import com.okane.entity.Notification;
import com.okane.entity.User;
import com.okane.entity.enums.TypeNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUtilisateurOrderByDateEnvoiDesc(User user);
    @Query("SELECT DISTINCT new com.okane.dto.responseDto.BroadcastNotificationResponse(" +
            "n.type, n.canal, n.contenu, n.dateEnvoi) " +
            "FROM Notification n " +
            "WHERE n.type IN :broadcastTypes " +
            "ORDER BY n.dateEnvoi DESC")
    List<BroadcastNotificationResponse> findAllBroadcasts(@Param("broadcastTypes") List<TypeNotification> broadcastTypes);
}
