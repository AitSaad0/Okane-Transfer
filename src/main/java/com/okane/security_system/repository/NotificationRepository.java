package com.okane.security_system.repository;

import com.okane.network_users.bean.User;
import com.okane.security_system.bean.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUtilisateurOrderByDateEnvoiDesc(User user);
}
