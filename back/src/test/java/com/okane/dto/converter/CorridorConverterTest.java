package com.okane.dto.converter;

import com.okane.dto.requestDto.CorridorRequestDTO;
import com.okane.dto.responseDto.CorridorResponseDTO;
import com.okane.entity.Corridor;
import com.okane.entity.Devise;
import com.okane.entity.Pays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CorridorConverterTest {

    private CorridorConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CorridorConverter();
    }

    @Test
    void toResponseDTO_shouldMapAllFields() {
        // Given
        Pays paysOrigine = Pays.builder().id(1L).nom("Sénégal").codeIso("SN").build();
        Pays paysDestination = Pays.builder().id(2L).nom("France").codeIso("FR").build();
        Devise deviseSource = Devise.builder().id(1L).code("XOF").symbole("CFA").build();
        Devise deviseDestination = Devise.builder().id(2L).code("EUR").symbole("€").build();

        Corridor corridor = Corridor.builder()
                .id(1L)
                .tauxChange(new BigDecimal("655.957"))
                .actif(true)
                .paysOrigine(paysOrigine)
                .paysDestination(paysDestination)
                .deviseSource(deviseSource)
                .deviseDestination(deviseDestination)
                .build();

        // When
        CorridorResponseDTO result = converter.toResponseDTO(corridor);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("655.957"), result.getTauxChange());
        assertTrue(result.getActif());
        assertEquals(1L, result.getPaysOrigineId());
        assertEquals("Sénégal", result.getPaysOrigineNom());
        assertEquals(2L, result.getPaysDestinationId());
        assertEquals("France", result.getPaysDestinationNom());
        assertEquals(1L, result.getDeviseSourceId());
        assertEquals("XOF", result.getDeviseSourceCode());
        assertEquals("CFA", result.getDeviseSourceSymbole());
        assertEquals(2L, result.getDeviseDestinationId());
        assertEquals("EUR", result.getDeviseDestinationCode());
        assertEquals("€", result.getDeviseDestinationSymbole());
    }

    @Test
    void toResponseDTO_shouldReturnNull_whenCorridorIsNull() {
        assertNull(converter.toResponseDTO(null));
    }

    @Test
    void toResponseDTO_shouldHandleNullRelations() {
        Corridor corridor = Corridor.builder()
                .id(1L)
                .tauxChange(new BigDecimal("655.957"))
                .actif(true)
                .build();

        CorridorResponseDTO result = converter.toResponseDTO(corridor);

        assertNotNull(result);
        assertNull(result.getPaysOrigineId());
        assertNull(result.getDeviseSourceCode());
    }

    @Test
    void toEntity_shouldCreateCorridor() {
        // Given
        CorridorRequestDTO dto = CorridorRequestDTO.builder()
                .tauxChange(new BigDecimal("655.957"))
                .actif(true)
                .build();

        Pays paysOrigine = Pays.builder().id(1L).build();
        Pays paysDestination = Pays.builder().id(2L).build();
        Devise deviseSource = Devise.builder().id(1L).build();
        Devise deviseDestination = Devise.builder().id(2L).build();

        // When
        Corridor result = converter.toEntity(dto, paysOrigine, paysDestination, deviseSource, deviseDestination);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("655.957"), result.getTauxChange());
        assertTrue(result.getActif());
        assertEquals(paysOrigine, result.getPaysOrigine());
        assertEquals(deviseDestination, result.getDeviseDestination());
    }

    @Test
    void toEntity_shouldReturnNull_whenDtoIsNull() {
        assertNull(converter.toEntity(null, null, null, null, null));
    }

    @Test
    void toEntity_shouldDefaultActifToTrue_whenNull() {
        CorridorRequestDTO dto = CorridorRequestDTO.builder()
                .tauxChange(new BigDecimal("655.957"))
                .build();

        Corridor result = converter.toEntity(dto, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.getActif());
    }

    @Test
    void updateEntityFromDTO_shouldUpdateAllFields() {
        // Given
        Corridor corridor = Corridor.builder()
                .id(1L)
                .tauxChange(new BigDecimal("600.000"))
                .actif(false)
                .build();

        CorridorRequestDTO dto = CorridorRequestDTO.builder()
                .tauxChange(new BigDecimal("700.000"))
                .actif(true)
                .build();

        Pays newPaysOrigine = Pays.builder().id(3L).build();
        Pays newPaysDestination = Pays.builder().id(4L).build();
        Devise newDeviseSource = Devise.builder().id(3L).build();
        Devise newDeviseDestination = Devise.builder().id(4L).build();

        // When
        converter.updateEntityFromDTO(corridor, dto, newPaysOrigine, newPaysDestination, newDeviseSource, newDeviseDestination);

        // Then
        assertEquals(new BigDecimal("700.000"), corridor.getTauxChange());
        assertTrue(corridor.getActif());
        assertEquals(newPaysOrigine, corridor.getPaysOrigine());
        assertEquals(newDeviseDestination, corridor.getDeviseDestination());
    }

    @Test
    void updateEntityFromDTO_shouldNotUpdateNullFields() {
        Corridor corridor = Corridor.builder()
                .id(1L)
                .tauxChange(new BigDecimal("600.000"))
                .actif(false)
                .build();

        converter.updateEntityFromDTO(corridor, null, null, null, null, null);

        assertEquals(new BigDecimal("600.000"), corridor.getTauxChange());
        assertFalse(corridor.getActif());
    }
}