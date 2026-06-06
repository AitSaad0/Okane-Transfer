package com.okane.dto.converter;

import com.okane.dto.requestDto.GrilleTarifaireRequestDTO;
import com.okane.dto.responseDto.GrilleTarifaireResponseDTO;
import com.okane.dto.responseDto.SimulationResponseDTO;
import com.okane.entity.Corridor;
import com.okane.entity.Devise;
import com.okane.entity.GrilleTarifaire;
import com.okane.entity.Pays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GrilleTarifaireConverterTest {

    private GrilleTarifaireConverter converter;

    @BeforeEach
    void setUp() {
        converter = new GrilleTarifaireConverter();
    }

    @Test
    void toResponseDTO_shouldMapAllFields() {
        Pays paysOrigine = Pays.builder().id(1L).nom("Sénégal").codeIso("SN").build();
        Pays paysDestination = Pays.builder().id(2L).nom("France").codeIso("FR").build();
        Devise deviseSource = Devise.builder().id(1L).code("XOF").build();
        Devise deviseDestination = Devise.builder().id(2L).code("EUR").build();

        Corridor corridor = Corridor.builder()
                .id(1L)
                .paysOrigine(paysOrigine)
                .paysDestination(paysDestination)
                .deviseSource(deviseSource)
                .deviseDestination(deviseDestination)
                .build();

        GrilleTarifaire grille = GrilleTarifaire.builder()
                .id(1L)
                .montantMin(new BigDecimal("0"))
                .montantMax(new BigDecimal("500"))
                .fraisFixe(new BigDecimal("25"))
                .pourcentageFrais(0.0)
                .partAgence(70.0)
                .corridor(corridor)
                .build();

        GrilleTarifaireResponseDTO result = converter.toResponseDTO(grille);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("0"), result.getMontantMin());
        assertEquals(new BigDecimal("500"), result.getMontantMax());
        assertEquals(new BigDecimal("25"), result.getFraisFixe());
        assertEquals(0.0, result.getPourcentageFrais());
        assertEquals(70.0, result.getPartAgence());
        assertEquals(1L, result.getCorridorId());
        assertEquals("Sénégal", result.getCorridorPaysOrigineNom());
        assertEquals("France", result.getCorridorPaysDestinationNom());
        assertEquals("XOF", result.getCorridorDeviseSourceCode());
        assertEquals("EUR", result.getCorridorDeviseDestinationCode());
    }

    @Test
    void toResponseDTO_shouldReturnNull_whenGrilleIsNull() {
        assertNull(converter.toResponseDTO(null));
    }

    @Test
    void toEntity_shouldCreateGrille() {
        GrilleTarifaireRequestDTO dto = GrilleTarifaireRequestDTO.builder()
                .montantMin(new BigDecimal("0"))
                .montantMax(new BigDecimal("500"))
                .fraisFixe(new BigDecimal("25"))
                .pourcentageFrais(0.0)
                .partAgence(70.0)
                .build();

        Corridor corridor = Corridor.builder().id(1L).build();

        GrilleTarifaire result = converter.toEntity(dto, corridor);

        assertNotNull(result);
        assertEquals(new BigDecimal("0"), result.getMontantMin());
        assertEquals(new BigDecimal("500"), result.getMontantMax());
        assertEquals(new BigDecimal("25"), result.getFraisFixe());
        assertEquals(0.0, result.getPourcentageFrais());
        assertEquals(70.0, result.getPartAgence());
        assertEquals(corridor, result.getCorridor());
    }

    @Test
    void toEntity_shouldReturnNull_whenDtoIsNull() {
        assertNull(converter.toEntity(null, null));
    }

    @Test
    void updateEntityFromDTO_shouldUpdateFields() {
        GrilleTarifaire grille = GrilleTarifaire.builder()
                .id(1L)
                .montantMin(new BigDecimal("0"))
                .montantMax(new BigDecimal("500"))
                .fraisFixe(new BigDecimal("25"))
                .pourcentageFrais(0.0)
                .partAgence(70.0)
                .build();

        GrilleTarifaireRequestDTO dto = GrilleTarifaireRequestDTO.builder()
                .montantMin(new BigDecimal("100"))
                .montantMax(new BigDecimal("1000"))
                .fraisFixe(new BigDecimal("50"))
                .pourcentageFrais(2.5)
                .partAgence(60.0)
                .build();

        converter.updateEntityFromDTO(grille, dto);

        assertEquals(new BigDecimal("100"), grille.getMontantMin());
        assertEquals(new BigDecimal("1000"), grille.getMontantMax());
        assertEquals(new BigDecimal("50"), grille.getFraisFixe());
        assertEquals(2.5, grille.getPourcentageFrais());
        assertEquals(60.0, grille.getPartAgence());
    }

    @Test
    void updateEntityFromDTO_shouldNotUpdateNullFields() {
        GrilleTarifaire grille = GrilleTarifaire.builder()
                .id(1L)
                .montantMin(new BigDecimal("0"))
                .fraisFixe(new BigDecimal("25"))
                .build();

        converter.updateEntityFromDTO(grille, null);

        assertEquals(new BigDecimal("0"), grille.getMontantMin());
        assertEquals(new BigDecimal("25"), grille.getFraisFixe());
    }

    @Test
    void toSimulationDTO_shouldCalculateCorrectly() {
        Pays paysOrigine = Pays.builder().id(1L).nom("Sénégal").build();
        Pays paysDestination = Pays.builder().id(2L).nom("France").build();

        Corridor corridor = Corridor.builder()
                .id(1L)
                .paysOrigine(paysOrigine)
                .paysDestination(paysDestination)
                .build();

        GrilleTarifaire grille = GrilleTarifaire.builder()
                .id(1L)
                .fraisFixe(new BigDecimal("25"))
                .pourcentageFrais(2.0)
                .partAgence(70.0)
                .corridor(corridor)
                .build();

        BigDecimal montant = new BigDecimal("1000");

        SimulationResponseDTO result = converter.toSimulationDTO(montant, grille, corridor);

        assertNotNull(result);
        assertEquals(new BigDecimal("1000"), result.getMontantEnvoye());
        assertEquals(new BigDecimal("25"), result.getFraisFixe());
        assertEquals(new BigDecimal("20.00"), result.getFraisVariable());
        assertEquals(new BigDecimal("45.00"), result.getFraisTotal());
        assertEquals(new BigDecimal("955.00"), result.getMontantRecu());
        assertEquals(70.0, result.getPartAgence());
        assertEquals(30.0, result.getPartCentrale());
        assertEquals("Sénégal → France", result.getCorridorDescription());
    }

    @Test
    void toSimulationDTO_shouldReturnNull_whenNullParams() {
        assertNull(converter.toSimulationDTO(null, null, null));
    }
}