package com.okane.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kyc_alert")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KycAlert {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType; // "WATCHLIST_MATCH" | "INVALID_DOCUMENT"

    @Column(nullable = false)
    private String severity; // "HIGH" | "MEDIUM" | "LOW"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "subject_name")
    private String subjectName;

    @Column(name = "subject_id_number")
    private String subjectIdNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 20)
    private String status; // "OPEN" | "RESOLVED"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;
}