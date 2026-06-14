package com.okane.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "compliance_threshold")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sar_threshold", nullable = false, precision = 19, scale = 2)
    private BigDecimal sarThreshold;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}