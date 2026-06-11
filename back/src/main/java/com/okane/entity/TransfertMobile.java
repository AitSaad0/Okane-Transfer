package com.okane.entity;

import com.okane.entity.enums.OperateurMobile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transfert_mobile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransfertMobile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "operateur", nullable = false)
    private OperateurMobile operateur;

    @Column(name = "numero_destinataire", nullable = false)
    private String numeroDestinataire;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfert_id", nullable = false, unique = true)
    private Transfert transfert;

}
