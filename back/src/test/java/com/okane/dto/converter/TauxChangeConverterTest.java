package com.okane.dto.converter;

import com.okane.dto.responseDto.TauxChangeResponseDTO;
import com.okane.entity.Corridor;
import com.okane.entity.Devise;
import com.okane.entity.Pays;
import com.okane.entity.TauxChangeHistorique;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TauxChangeConverterTest {

    private TauxChangeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TauxChangeConverter();
    }

    @Test
    void toCurrentRateDTO_shouldMapAllFields() {
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

        TauxChangeResponseDTO result = converter.toCurrentRateDTO(corridor);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("655.957"), result.getTaux());
        assertEquals("ACTUEL", result.getSource());
        assertEquals(1L, result.getCorridorId());
        assertEquals("Sénégal", result.getPaysOrigineNom());
        assertEquals("France", result.getPaysDestinationNom());
        assertEquals("XOF", result.getDeviseSourceCode());
        assertEquals("EUR", result.getDeviseDestinationCode());
        assertEquals("CFA", result.getDeviseSourceSymbole());
        assertEquals("€", result.getDeviseDestinationSymbole());
    }

    @Test
    void toCurrentRateDTO_shouldReturnNull_whenCorridorIsNull() {
        assertNull(converter.toCurrentRateDTO(null));
    }

    @Test
    void toHistoryDTO_shouldMapAllFields() {
        Pays paysOrigine = Pays.builder().id(1L).nom("Sénégal").codeIso("SN").build();
        Pays paysDestination = Pays.builder().id(2L).nom("France").codeIso("FR").build();
        Devise deviseSource = Devise.builder().id(1L).code("XOF").symbole("CFA").build();
        Devise deviseDestination = Devise.builder().id(2L).code("EUR").symbole("€").build();

        Corridor corridor = Corridor.builder()
                .id(1L)
                .paysOrigine(paysOrigine)
                .paysDestination(paysDestination)
                .deviseSource(deviseSource)
                .deviseDestination(deviseDestination)
                .build();

        TauxChangeHistorique historique = TauxChangeHistorique.builder()
                .id(1L)
                .tauxAncien(new BigDecimal("650.000"))
                .tauxNouveau(new BigDecimal("655.957"))
                .source("API_EXTERNE")
                .dateChangement(LocalDateTime.of(2024, 1, 15, 10, 30))
                .corridor(corridor)
                .build();

        TauxChangeResponseDTO result = converter.toHistoryDTO(historique);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("655.957"), result.getTaux());
        assertEquals("API_EXTERNE", result.getSource());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30), result.getDateMiseAJour());
        assertEquals("Sénégal", result.getPaysOrigineNom());
        assertEquals("XOF", result.getDeviseSourceCode());
    }

    @Test
    void toHistoryDTO_shouldReturnNull_whenHistoriqueIsNull() {
        assertNull(converter.toHistoryDTO(null));
    }
}