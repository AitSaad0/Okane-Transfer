package com.okane.dto.converter;

import com.okane.dto.responseDto.UserResponseDTO;
import com.okane.entity.Agence;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserConverterTest {

    private UserConverter userConverter;

    @BeforeEach
    void setUp() {
        userConverter = new UserConverter();
    }

    // ─────────────────────────────────────────────────────────────
    // toResponseDto — cas nominal avec agence
    // ─────────────────────────────────────────────────────────────

    @Test
    void toResponseDto_shouldMapAllFieldsCorrectly() {
        Agence agence = Agence.builder()
                .id(5L)
                .nom("Agence Casablanca Centre")
                .build();

        User user = User.builder()
                .id(1L)
                .email("agent@okane.com")
                .nom("Dupont")
                .prenom("Marie")
                .telephone("+212600000001")
                .role(Role.AGENT)
                .active(true)
                .agence(agence)
                .build();

        UserResponseDTO dto = userConverter.toResponseDto(user);

        assertNotNull(dto);
        assertEquals(1L,                     dto.getId());
        assertEquals("agent@okane.com",      dto.getEmail());
        assertEquals("Dupont",               dto.getNom());
        assertEquals("Marie",                dto.getPrenom());
        assertEquals("+212600000001",        dto.getTelephone());
        assertEquals(Role.AGENT,             dto.getRole());
        assertTrue(dto.getActive());
        assertEquals(5L,                     dto.getAgenceId());
        assertEquals("Agence Casablanca Centre", dto.getAgenceNom());
    }

    // ─────────────────────────────────────────────────────────────
    // toResponseDto — agence null
    // ─────────────────────────────────────────────────────────────

    @Test
    void toResponseDto_shouldReturnNullAgenceFieldsWhenAgenceIsNull() {
        User user = User.builder()
                .id(2L)
                .email("admin@okane.com")
                .nom("Martin")
                .prenom("Jean")
                .telephone("+212600000002")
                .role(Role.ADMIN)
                .active(true)
                .agence(null)
                .build();

        UserResponseDTO dto = userConverter.toResponseDto(user);

        assertNotNull(dto);
        assertNull(dto.getAgenceId());
        assertNull(dto.getAgenceNom());
        assertEquals(2L,             dto.getId());
        assertEquals("admin@okane.com", dto.getEmail());
        assertEquals(Role.ADMIN,     dto.getRole());
    }

    // ─────────────────────────────────────────────────────────────
    // toResponseDto — user inactif
    // ─────────────────────────────────────────────────────────────

    @Test
    void toResponseDto_shouldMapActiveAsFalseWhenUserIsInactive() {
        Agence agence = Agence.builder()
                .id(3L)
                .nom("Agence Rabat")
                .build();

        User user = User.builder()
                .id(3L)
                .email("suspended@okane.com")
                .nom("Alami")
                .prenom("Youssef")
                .telephone("+212600000003")
                .role(Role.AGENT)
                .active(false)
                .agence(agence)
                .build();

        UserResponseDTO dto = userConverter.toResponseDto(user);

        assertNotNull(dto);
        assertFalse(dto.getActive());
        assertEquals(3L, dto.getId());
    }

    // ─────────────────────────────────────────────────────────────
    // toResponseDto — telephone null
    // ─────────────────────────────────────────────────────────────

    @Test
    void toResponseDto_shouldHandleNullTelephone() {
        User user = User.builder()
                .id(4L)
                .email("client@okane.com")
                .nom("Benali")
                .prenom("Fatima")
                .telephone(null)
                .role(Role.CLIENT)
                .active(true)
                .agence(null)
                .build();

        UserResponseDTO dto = userConverter.toResponseDto(user);

        assertNotNull(dto);
        assertNull(dto.getTelephone());
        assertEquals(4L,              dto.getId());
        assertEquals(Role.CLIENT,     dto.getRole());
        assertEquals("client@okane.com", dto.getEmail());
    }

    // ─────────────────────────────────────────────────────────────
    // toResponseDto — tous les rôles
    // ─────────────────────────────────────────────────────────────

    @Test
    void toResponseDto_shouldMapRoleManager() {
        User user = User.builder()
                .id(5L)
                .email("manager@okane.com")
                .nom("Tazi")
                .prenom("Karim")
                .role(Role.MANAGER)
                .active(true)
                .agence(null)
                .build();

        UserResponseDTO dto = userConverter.toResponseDto(user);

        assertNotNull(dto);
        assertEquals(Role.MANAGER, dto.getRole());
        assertEquals(5L,           dto.getId());
    }
}