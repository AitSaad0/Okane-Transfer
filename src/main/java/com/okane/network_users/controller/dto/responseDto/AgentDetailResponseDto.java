package com.okane.network_users.controller.dto.responseDto;

import com.okane.shared.Role;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AgentDetailResponseDto {

    // Infos utilisateur
    private Long          id;
    private String        nom;
    private String        prenom;
    private String        email;
    private String        telephone;
    private Role          role;
    private Boolean       active;

    // Agence
    private Long          agenceId;
    private String        agenceNom;
    private String        agenceVille;

    // Caisse courante de l'agent
    private String        caisseId;         // UUID caisse
    private Boolean       caisseOuverte;    // true si OUVERTE
    private BigDecimal    soldeCaisse;      // solde courant
    private LocalDateTime dateOuvertureCaisse;
}