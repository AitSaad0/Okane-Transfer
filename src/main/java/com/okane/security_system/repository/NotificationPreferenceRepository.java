package com.okane.security_system.repository;


import com.okane.network_users.bean.User;
import com.okane.security_system.bean.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferenceRepository
        extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByUser(User user);
}
