package com.okane.security_system.bean;

import com.okane.clients_transfers.bean.Transfert;
import com.okane.network_users.bean.User;
import com.okane.shared.CanalNotification;
import com.okane.shared.TypeNotification;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypeNotification type;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false)
    private CanalNotification canal;

    @Column(name = "contenu", nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "date_envoi", nullable = false)
    private LocalDateTime dateEnvoi;

    @Column(name = "lu", nullable = false)
    private Boolean lu = false;

    // N notifications → 1 utilisateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    // N notifications → 1 transfert (concerne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfert_id")
    private Transfert transfert;

}
