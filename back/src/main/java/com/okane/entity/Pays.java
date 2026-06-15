package com.okane.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pays")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pays {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "code_iso", nullable = false, unique = true, length = 2)
    private String codeIso;

    @Column(name = "nom", nullable = false)
    private String nom;
}