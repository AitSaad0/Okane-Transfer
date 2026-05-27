package com.okane.security_system.bean;

import com.okane.network_users.bean.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "email_active", nullable = false)
    private Boolean emailActive = true;

    @Column(name = "push_active", nullable = false)
    private Boolean pushActive = true;

    @Column(name = "sms_active", nullable = false)
    private Boolean smsActive = false;
}