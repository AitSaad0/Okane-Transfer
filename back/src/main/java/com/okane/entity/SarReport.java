package com.okane.entity;

import com.okane.entity.Transfert;
import com.okane.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sar_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SarReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_code", nullable = false, unique = true)
    private String referenceCode;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "threshold_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal thresholdAmount;

    @Column(name = "transfer_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal transferAmount;

    @Column(name = "status", nullable = false)
    private String status;
    // OPEN | REVIEWING | CLOSED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfert_id", nullable = false)
    private Transfert transfert;
}