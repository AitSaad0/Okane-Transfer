package com.okane.dto.converter;

import com.okane.dto.requestDto.GrilleTarifaireRequestDTO;
import com.okane.dto.responseDto.GrilleTarifaireResponseDTO;
import com.okane.dto.responseDto.SimulationResponseDTO;
import com.okane.entity.Corridor;
import com.okane.entity.GrilleTarifaire;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class GrilleTarifaireConverter {

    public GrilleTarifaireResponseDTO toResponseDTO(GrilleTarifaire grille) {
        if (grille == null) return null;

        Corridor corridor = grille.getCorridor();

        return GrilleTarifaireResponseDTO.builder()
                .id(grille.getId())
                .montantMin(grille.getMontantMin())
                .montantMax(grille.getMontantMax())
                .fraisFixe(grille.getFraisFixe())
                .pourcentageFrais(grille.getPourcentageFrais())
                .partAgence(grille.getPartAgence())
                .corridorId(corridor != null ? corridor.getId() : null)
                .corridorPaysOrigineNom(corridor != null && corridor.getPaysOrigine() != null ? corridor.getPaysOrigine().getNom() : null)
                .corridorPaysDestinationNom(corridor != null && corridor.getPaysDestination() != null ? corridor.getPaysDestination().getNom() : null)
                .corridorDeviseSourceCode(corridor != null && corridor.getDeviseSource() != null ? corridor.getDeviseSource().getCode() : null)
                .corridorDeviseDestinationCode(corridor != null && corridor.getDeviseDestination() != null ? corridor.getDeviseDestination().getCode() : null)
                .build();
    }

    public GrilleTarifaire toEntity(GrilleTarifaireRequestDTO dto, Corridor corridor) {
        if (dto == null) return null;

        return GrilleTarifaire.builder()
                .corridor(corridor)
                .montantMin(dto.getMontantMin())
                .montantMax(dto.getMontantMax())
                .fraisFixe(dto.getFraisFixe())
                .pourcentageFrais(dto.getPourcentageFrais())
                .partAgence(dto.getPartAgence())
                .build();
    }

    public void updateEntityFromDTO(GrilleTarifaire grille, GrilleTarifaireRequestDTO dto) {
        if (dto == null || grille == null) return;

        if (dto.getMontantMin() != null) grille.setMontantMin(dto.getMontantMin());
        if (dto.getMontantMax() != null) grille.setMontantMax(dto.getMontantMax());
        if (dto.getFraisFixe() != null) grille.setFraisFixe(dto.getFraisFixe());
        if (dto.getPourcentageFrais() != null) grille.setPourcentageFrais(dto.getPourcentageFrais());
        if (dto.getPartAgence() != null) grille.setPartAgence(dto.getPartAgence());
    }

    public SimulationResponseDTO toSimulationDTO(BigDecimal montant, GrilleTarifaire grille, Corridor corridor) {
        if (grille == null || montant == null) return null;

        BigDecimal fraisVariable = montant
                .multiply(BigDecimal.valueOf(grille.getPourcentageFrais()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal fraisTotal = grille.getFraisFixe().add(fraisVariable);
        BigDecimal montantRecu = montant.subtract(fraisTotal);

        BigDecimal partAgenceMontant = fraisTotal
                .multiply(BigDecimal.valueOf(grille.getPartAgence()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return SimulationResponseDTO.builder()
                .montantEnvoye(montant)
                .fraisFixe(grille.getFraisFixe())
                .fraisVariable(fraisVariable)
                .fraisTotal(fraisTotal)
                .montantRecu(montantRecu)
                .partAgence(grille.getPartAgence())
                .partCentrale(100.0 - grille.getPartAgence())
                .corridorDescription(corridor != null ?
                        corridor.getPaysOrigine().getNom() + " → " + corridor.getPaysDestination().getNom() : null)
                .message("Simulation basée sur la grille tarifaire applicable")
                .build();
    }
}