package com.okane.clients_transfers.bean;

import com.okane.network_users.bean.Agence;
import com.okane.pricing_configuration.bean.Corridor;
import com.okane.network_users.bean.User;
import com.okane.shared.StatutTransfert;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfert")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfert {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code_retrait", nullable = false, unique = true)
    private String codeRetrait;

    @Column(name = "montant_envoye", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantEnvoye;

    @Column(name = "frais", nullable = false, precision = 19, scale = 2)
    private BigDecimal frais;

    @Column(name = "montant_net", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantNet;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutTransfert statut;

    @Column(name = "est_suspect", nullable = false)
    private Boolean estSuspect = false;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediteur_id", nullable = false)
    private Client expediteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiaire_id", nullable = false)
    private Client beneficiaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_envoi_id", nullable = false)
    private Agence agenceEnvoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_paiement_id")
    private Agence agencePaiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_envoi_id", nullable = false)
    private User agentEnvoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_paiement_id")
    private User agentPaiement;
}