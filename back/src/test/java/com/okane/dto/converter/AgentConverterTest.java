package com.okane.dto.converter;

import com.okane.dto.responseDto.AgentDetailResponseDto;
import com.okane.entity.Agence;
import com.okane.entity.Caisse;
import com.okane.entity.Pays;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import com.okane.entity.enums.StatutCaisse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgentConverterTest {

    private AgentConverter agentConverter;

    @BeforeEach
    void setUp() {
        agentConverter = new AgentConverter();
    }

    // ─────────────────────────────────────────────────────────────
    // toDto — cas nominal avec caisse OUVERTE
    // ─────────────────────────────────────────────────────────────

    @Test
    void toDto_shouldMapAllFieldsWithCaisseOuverte() {
        Agence agence = Agence.builder()
                .id(5L)
                .nom("Agence Casablanca Centre")
                .ville("Casablanca")
                .build();

        LocalDateTime dateOuverture = LocalDateTime.of(2026, 5, 30, 8, 0);

        Caisse caisseOuverte = Caisse.builder()
                .id(100L)
                .soldeCourant(new BigDecimal("15000.00"))
                .dateOuverture(dateOuverture)
                .statut(StatutCaisse.OUVERTE)
                .build();

        User user = User.builder()
                .id(1L)
                .nom("Alami")
                .prenom("Youssef")
                .email("youssef.alami@okane.com")
                .telephone("+212600000010")
                .role(Role.AGENT)
                .active(true)
                .agence(agence)
                .caisses(List.of(caisseOuverte))
                .build();

        AgentDetailResponseDto dto = agentConverter.toDto(user);

        assertNotNull(dto);
        assertEquals(1L,                          dto.getId());
        assertEquals("Alami",                     dto.getNom());
        assertEquals("Youssef",                   dto.getPrenom());
        assertEquals("youssef.alami@okane.com",   dto.getEmail());
        assertEquals("+212600000010",              dto.getTelephone());
        assertEquals(Role.AGENT,                   dto.getRole());
        assertTrue(dto.getActive());
        assertEquals(5L,                           dto.getAgenceId());
        assertEquals("Agence Casablanca Centre",   dto.getAgenceNom());
        assertEquals("Casablanca",                 dto.getAgenceVille());
        assertEquals("100",                        dto.getCaisseId());
        assertTrue(dto.getCaisseOuverte());
        assertEquals(0, new BigDecimal("15000.00").compareTo(dto.getSoldeCaisse()));
        assertEquals(dateOuverture,                dto.getDateOuvertureCaisse());
    }

    // ─────────────────────────────────────────────────────────────
    // toDto — caisse FERMEE uniquement (pas de caisse active)
    // ─────────────────────────────────────────────────────────────

    @Test
    void toDto_shouldReturnNullCaisseFieldsWhenNoCaisseOuverte() {
        Agence agence = Agence.builder()
                .id(5L)
                .nom("Agence Casablanca Centre")
                .ville("Casablanca")
                .build();

        Caisse caisseFermee = Caisse.builder()
                .id(101L)
                .soldeCourant(new BigDecimal("8000.00"))
                .dateOuverture(LocalDateTime.of(2026, 5, 29, 8, 0))
                .statut(StatutCaisse.FERMEE)
                .build();

        User user = User.builder()
                .id(2L)
                .nom("Tazi")
                .prenom("Karim")
                .email("karim.tazi@okane.com")
                .telephone("+212600000011")
                .role(Role.AGENT)
                .active(true)
                .agence(agence)
                .caisses(List.of(caisseFermee))
                .build();

        AgentDetailResponseDto dto = agentConverter.toDto(user);

        assertNotNull(dto);
        assertNull(dto.getCaisseId());
        assertFalse(dto.getCaisseOuverte());
        assertNull(dto.getSoldeCaisse());
        assertNull(dto.getDateOuvertureCaisse());
    }

    // ─────────────────────────────────────────────────────────────
    // toDto — liste caisses null
    // ─────────────────────────────────────────────────────────────

    @Test
    void toDto_shouldReturnNullCaisseFieldsWhenCaissesIsNull() {
        Agence agence = Agence.builder()
                .id(5L)
                .nom("Agence Casablanca Centre")
                .ville("Casablanca")
                .build();

        User user = User.builder()
                .id(3L)
                .nom("Idrissi")
                .prenom("Sara")
                .email("sara.idrissi@okane.com")
                .telephone("+212600000012")
                .role(Role.AGENT)
                .active(true)
                .agence(agence)
                .caisses(null)
                .build();

        AgentDetailResponseDto dto = agentConverter.toDto(user);

        assertNotNull(dto);
        assertNull(dto.getCaisseId());
        assertFalse(dto.getCaisseOuverte());
        assertNull(dto.getSoldeCaisse());
        assertNull(dto.getDateOuvertureCaisse());
    }

    // ─────────────────────────────────────────────────────────────
    // toDto — plusieurs caisses, une seule OUVERTE
    // ─────────────────────────────────────────────────────────────

    @Test
    void toDto_shouldPickOnlyTheCaisseOuverteWhenMultipleCaisses() {
        Agence agence = Agence.builder()
                .id(5L)
                .nom("Agence Casablanca Centre")
                .ville("Casablanca")
                .build();

        Caisse caisseFermee = Caisse.builder()
                .id(200L)
                .soldeCourant(new BigDecimal("5000.00"))
                .dateOuverture(LocalDateTime.of(2026, 5, 28, 8, 0))
                .statut(StatutCaisse.FERMEE)
                .build();

        LocalDateTime dateOuverture = LocalDateTime.of(2026, 5, 30, 8, 0);
        Caisse caisseOuverte = Caisse.builder()
                .id(201L)
                .soldeCourant(new BigDecimal("20000.00"))
                .dateOuverture(dateOuverture)
                .statut(StatutCaisse.OUVERTE)
                .build();

        User user = User.builder()
                .id(4L)
                .nom("Mansouri")
                .prenom("Amine")
                .email("amine.mansouri@okane.com")
                .telephone("+212600000013")
                .role(Role.AGENT)
                .active(true)
                .agence(agence)
                .caisses(List.of(caisseFermee, caisseOuverte))
                .build();

        AgentDetailResponseDto dto = agentConverter.toDto(user);

        assertNotNull(dto);
        assertEquals("201",  dto.getCaisseId());
        assertTrue(dto.getCaisseOuverte());
        assertEquals(0, new BigDecimal("20000.00").compareTo(dto.getSoldeCaisse()));
        assertEquals(dateOuverture, dto.getDateOuvertureCaisse());
    }

    // ─────────────────────────────────────────────────────────────
    // toDto — agence null
    // ─────────────────────────────────────────────────────────────

    @Test
    void toDto_shouldReturnNullAgenceFieldsWhenAgenceIsNull() {
        User user = User.builder()
                .id(5L)
                .nom("Bennani")
                .prenom("Omar")
                .email("omar.bennani@okane.com")
                .telephone("+212600000014")
                .role(Role.AGENT)
                .active(false)
                .agence(null)
                .caisses(List.of())
                .build();

        AgentDetailResponseDto dto = agentConverter.toDto(user);

        assertNotNull(dto);
        assertNull(dto.getAgenceId());
        assertNull(dto.getAgenceNom());
        assertNull(dto.getAgenceVille());
        assertEquals(5L,      dto.getId());
        assertFalse(dto.getActive());
    }
}