package com.okane.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "nom",nullable = false)
    private String nom;

    @Column(name = "prenom",nullable = false)
    private String prenom;

    @Column(name = "num_piece_identite", nullable = false, unique = true)
    private String numPieceIdentite;

    @Column(name = "telephone",nullable = false)
    private String telephone;

    @Column(name = "est_sur_liste_surveillance", nullable = false)
    private Boolean estSurListeSurveillance = false;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private LocalDate dateNaissance;

    @Column(nullable = false)
    private String email;

    // compte en ligne : relation vers User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pays_id", nullable = true)
    private Pays pays;

}