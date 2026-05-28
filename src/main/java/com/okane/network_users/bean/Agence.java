package com.okane.network_users.bean;

import com.okane.geographic_monetary_reference.bean.Pays;
import com.okane.shared.StatutAgence;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "agence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String adresse;

    @Column(name = "plafond_journalier", nullable = false, precision = 19, scale = 2)
    private BigDecimal plafondJournalier;

    @Column(nullable = false)
    private String ville;

    @Column(name = "code_postal")
    private String codePostal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAgence statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pays_id", nullable = false)
    private Pays pays;

    @OneToMany(mappedBy = "agence", cascade = CascadeType.ALL)
    private List<User> users;

    @OneToMany(mappedBy = "agence", cascade = CascadeType.ALL)
    private List<Caisse> caisses;

}