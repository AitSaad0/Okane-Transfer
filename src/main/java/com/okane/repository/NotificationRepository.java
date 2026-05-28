package com.okane.repository;

import com.okane.entity.Notification;
import com.okane.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUtilisateurOrderByDateEnvoiDesc(User user);
}
