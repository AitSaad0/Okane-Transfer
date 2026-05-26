package com.okane.security_system.bean;

import com.okane.clients_transfers.bean.Transfert;
import com.okane.network_users.bean.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "journal_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "action",nullable = false)
    private String action;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "ip_address",nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    // N entrées journal → 1 utilisateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;


    // N entrées journal → 1 transfert (optionnel — certaines actions ne concernent pas un transfert)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfert_id")
    private Transfert transfert;
}