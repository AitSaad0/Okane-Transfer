package com.okane.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "watchlist_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistEntry {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "id_number", unique = true)
    private String idNumber;

    @Column(nullable = false, length = 50)
    private String source; // e.g. "OFAC"

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "added_by", nullable = false)
    private String addedBy; // admin email
}