package com.okane.dto.converter;

import com.okane.dto.responseDto.AgenceResponseDto;
import com.okane.entity.Agence;
import com.okane.entity.Pays;
import com.okane.entity.enums.StatutAgence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AgenceConverterTest {

    private AgenceConverter agenceConverter;

    @BeforeEach
    void setUp() {
        agenceConverter = new AgenceConverter();
    }

    // ─────────────────────────────────────────────────────────────
    // toDto — cas nominal
    // ─────────────────────────────────────────────────────────────

    @Test
    void toDto_shouldMapAllFieldsCorrectly() {
        Pays pays = Pays.builder()
                .id(1L)
                .nom("Maroc")
                .codeIso("MA")
                .build();

        Agence agence = Agence.builder()
                .id(10L)
                .nom("Agence Casablanca Centre")
                .adresse("12 Rue Hassan II")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("50000.00"))
                .statut(StatutAgence.ACTIVE)
                .pays(pays)
                .build();

        AgenceResponseDto dto = agenceConverter.toDto(agence);

        assertNotNull(dto);
        assertEquals(10L,                              dto.getId());
        assertEquals("Agence Casablanca Centre",       dto.getNom());
        assertEquals("12 Rue Hassan II",               dto.getAdresse());
        assertEquals("Casablanca",                     dto.getVille());
        assertEquals("20000",                          dto.getCodePostal());
        assertEquals(0, new BigDecimal("50000.00").compareTo(dto.getPlafondJournalier()));
        assertEquals(StatutAgence.ACTIVE,              dto.getStatut());
        assertEquals("Maroc",                          dto.getPaysNom());
        assertEquals("MA",                             dto.getPaysCode());
    }

    @Test
    void toDto_shouldMapStatutSuspendue() {
        Pays pays = Pays.builder().id(1L).nom("Maroc").codeIso("MA").build();

        Agence agence = Agence.builder()
                .id(11L)
                .nom("Agence Rabat")
                .adresse("5 Avenue Mohamed V")
                .ville("Rabat")
                .codePostal("10000")
                .plafondJournalier(new BigDecimal("30000.00"))
                .statut(StatutAgence.SUSPENDUE)
                .pays(pays)
                .build();

        AgenceResponseDto dto = agenceConverter.toDto(agence);

        assertNotNull(dto);
        assertEquals(StatutAgence.SUSPENDUE, dto.getStatut());
        assertEquals(11L, dto.getId());
    }

    // ─────────────────────────────────────────────────────────────
    // toDto — pays null
    // ─────────────────────────────────────────────────────────────

    @Test
    void toDto_shouldReturnNullPaysFieldsWhenPaysIsNull() {
        Agence agence = Agence.builder()
                .id(12L)
                .nom("Agence Sans Pays")
                .adresse("Adresse inconnue")
                .ville("Ville")
                .codePostal("00000")
                .plafondJournalier(new BigDecimal("10000.00"))
                .statut(StatutAgence.ACTIVE)
                .pays(null)
                .build();

        AgenceResponseDto dto = agenceConverter.toDto(agence);

        assertNotNull(dto);
        assertNull(dto.getPaysNom());
        assertNull(dto.getPaysCode());
        assertEquals(12L,               dto.getId());
        assertEquals("Agence Sans Pays", dto.getNom());
    }

    // ─────────────────────────────────────────────────────────────
    // toDto — champs optionnels null
    // ─────────────────────────────────────────────────────────────

    @Test
    void toDto_shouldHandleNullVilleAndCodePostal() {
        Pays pays = Pays.builder().id(1L).nom("Maroc").codeIso("MA").build();

        Agence agence = Agence.builder()
                .id(13L)
                .nom("Agence Minimale")
                .adresse("Adresse")
                .ville(null)
                .codePostal(null)
                .plafondJournalier(new BigDecimal("5000.00"))
                .statut(StatutAgence.ACTIVE)
                .pays(pays)
                .build();

        AgenceResponseDto dto = agenceConverter.toDto(agence);

        assertNotNull(dto);
        assertNull(dto.getVille());
        assertNull(dto.getCodePostal());
        assertEquals("Maroc", dto.getPaysNom());
        assertEquals("MA",    dto.getPaysCode());
    }
}