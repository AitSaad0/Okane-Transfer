package com.okane.entity;

import com.okane.entity.enums.CanalNotification;
import com.okane.entity.enums.TypeNotification;
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

    // N notifications → 1 utilisateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    // N notifications → 1 transfert (concerne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfert_id", nullable = false)
    private Transfert transfert;

}
