package com.okane.dto.converter;

import com.okane.dto.responseDto.ClientProfileResponseDto;
import com.okane.entity.Client;
import com.okane.entity.Pays;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ClientConverterTest {

    private ClientConverter clientConverter;

    @BeforeEach
    void setUp() {
        clientConverter = new ClientConverter();
    }

    // ─────────────────────────────────────────────────────────────
    // toProfileDto — cas nominal avec User et Pays
    // ─────────────────────────────────────────────────────────────

    @Test
    void toProfileDto_shouldMapAllFieldsCorrectly() {
        Pays pays = Pays.builder()
                .id(1L)
                .nom("Maroc")
                .codeIso("MA")
                .build();

        User user = User.builder()
                .id(10L)
                .email("fatima.benali@okane.com")
                .notificationEmail(true)
                .notificationSms(true)
                .notificationPush(false)
                .role(Role.CLIENT)
                .active(true)
                .build();

        Client client = Client.builder()
                .id(1L)
                .nom("Benali")
                .prenom("Fatima")
                .email("fatima.benali@okane.com")
                .telephone("+212600000020")
                .numPieceIdentite("AB123456")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .estSurListeSurveillance(false)
                .pays(pays)
                .user(user)
                .build();

        ClientProfileResponseDto dto = clientConverter.toProfileDto(client);

        assertNotNull(dto);
        assertEquals(1L,                         dto.getId());
        assertEquals("Benali",                   dto.getNom());
        assertEquals("Fatima",                   dto.getPrenom());
        assertEquals("fatima.benali@okane.com",  dto.getEmail());
        assertEquals("+212600000020",            dto.getTelephone());
        assertEquals("AB123456",                 dto.getNumPieceIdentite());
        assertEquals(LocalDate.of(1990, 5, 15),  dto.getDateNaissance());
        assertEquals("Maroc",                    dto.getPaysNom());
        assertEquals("MA",                       dto.getPaysCode());
        assertFalse(dto.getEstSurListeSurveillance());
        assertTrue(dto.getNotificationEmail());
        assertTrue(dto.getNotificationSms());
        assertFalse(dto.getNotificationPush());
    }

    // ─────────────────────────────────────────────────────────────
    // toProfileDto — pays null
    // ─────────────────────────────────────────────────────────────

    @Test
    void toProfileDto_shouldReturnNullPaysFieldsWhenPaysIsNull() {
        User user = User.builder()
                .id(11L)
                .notificationEmail(false)
                .notificationSms(false)
                .notificationPush(false)
                .build();

        Client client = Client.builder()
                .id(2L)
                .nom("Tazi")
                .prenom("Karim")
                .email("karim.tazi@okane.com")
                .telephone("+212600000021")
                .numPieceIdentite("CD789012")
                .estSurListeSurveillance(false)
                .pays(null)
                .user(user)
                .build();

        ClientProfileResponseDto dto = clientConverter.toProfileDto(client);

        assertNotNull(dto);
        assertNull(dto.getPaysNom());
        assertNull(dto.getPaysCode());
        assertEquals(2L,      dto.getId());
        assertEquals("Tazi",  dto.getNom());
    }

    // ─────────────────────────────────────────────────────────────
    // toProfileDto — user null (pas de compte en ligne)
    // ─────────────────────────────────────────────────────────────

    @Test
    void toProfileDto_shouldReturnNullNotificationFieldsWhenUserIsNull() {
        Pays pays = Pays.builder()
                .id(1L)
                .nom("Maroc")
                .codeIso("MA")
                .build();

        Client client = Client.builder()
                .id(3L)
                .nom("Idrissi")
                .prenom("Sara")
                .email("sara.idrissi@okane.com")
                .telephone("+212600000022")
                .numPieceIdentite("EF345678")
                .estSurListeSurveillance(false)
                .pays(pays)
                .user(null)
                .build();

        ClientProfileResponseDto dto = clientConverter.toProfileDto(client);

        assertNotNull(dto);
        assertNull(dto.getNotificationEmail());
        assertNull(dto.getNotificationSms());
        assertNull(dto.getNotificationPush());
        assertEquals(3L,       dto.getId());
        assertEquals("Maroc",  dto.getPaysNom());
    }

    // ─────────────────────────────────────────────────────────────
    // toProfileDto — estSurListeSurveillance true
    // ─────────────────────────────────────────────────────────────

    @Test
    void toProfileDto_shouldMapEstSurListeSurveillanceTrue() {
        Pays pays = Pays.builder().id(1L).nom("Maroc").codeIso("MA").build();

        User user = User.builder()
                .id(12L)
                .notificationEmail(true)
                .notificationSms(false)
                .notificationPush(true)
                .build();

        Client client = Client.builder()
                .id(4L)
                .nom("Mansouri")
                .prenom("Amine")
                .email("amine.mansouri@okane.com")
                .telephone("+212600000023")
                .numPieceIdentite("GH901234")
                .estSurListeSurveillance(true)
                .pays(pays)
                .user(user)
                .build();

        ClientProfileResponseDto dto = clientConverter.toProfileDto(client);

        assertNotNull(dto);
        assertTrue(dto.getEstSurListeSurveillance());
        assertTrue(dto.getNotificationEmail());
        assertFalse(dto.getNotificationSms());
        assertTrue(dto.getNotificationPush());
    }

    // ─────────────────────────────────────────────────────────────
    // toProfileDto — dateNaissance null
    // ─────────────────────────────────────────────────────────────

    @Test
    void toProfileDto_shouldHandleNullDateNaissance() {
        Pays pays = Pays.builder().id(1L).nom("Maroc").codeIso("MA").build();

        User user = User.builder()
                .id(13L)
                .notificationEmail(true)
                .notificationSms(true)
                .notificationPush(false)
                .build();

        Client client = Client.builder()
                .id(5L)
                .nom("Bennani")
                .prenom("Omar")
                .email("omar.bennani@okane.com")
                .telephone("+212600000024")
                .numPieceIdentite("IJ567890")
                .dateNaissance(null)
                .estSurListeSurveillance(false)
                .pays(pays)
                .user(user)
                .build();

        ClientProfileResponseDto dto = clientConverter.toProfileDto(client);

        assertNotNull(dto);
        assertNull(dto.getDateNaissance());
        assertEquals("Bennani", dto.getNom());
    }
}