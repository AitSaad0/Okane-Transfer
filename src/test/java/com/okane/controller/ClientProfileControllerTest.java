package com.okane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.okane.dto.requestDto.UpdateClientProfileRequestDto;
import com.okane.dto.responseDto.ClientActivityResponseDto;
import com.okane.dto.responseDto.ClientProfileResponseDto;
import com.okane.exception.GlobalExceptionHandler;
import com.okane.exception.ResourceNotFoundException;
import com.okane.pagination.PageResponseDto;
import com.okane.service.ClientProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientProfileControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // pour LocalDate et LocalDateTime

    @Mock
    private ClientProfileService clientProfileService;

    @InjectMocks
    private ClientProfileController clientProfileController;

    private ClientProfileResponseDto sampleProfile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(clientProfileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleProfile = ClientProfileResponseDto.builder()
                .id(1L)
                .nom("Benali")
                .prenom("Fatima")
                .email("fatima.benali@okane.com")
                .telephone("+212600000020")
                .numPieceIdentite("AB123456")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .paysNom("Maroc")
                .paysCode("MA")
                .estSurListeSurveillance(false)
                .notificationEmail(true)
                .notificationSms(true)
                .notificationPush(false)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/clients/profile
    // ─────────────────────────────────────────────────────────────

    @Test
    void getMyProfile_shouldReturn200WithAllFields() throws Exception {
        when(clientProfileService.getMyProfile(1L)).thenReturn(sampleProfile);

        mockMvc.perform(get("/api/v1/clients/profile")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nom").value("Benali"))
                .andExpect(jsonPath("$.prenom").value("Fatima"))
                .andExpect(jsonPath("$.email").value("fatima.benali@okane.com"))
                .andExpect(jsonPath("$.telephone").value("+212600000020"))
                .andExpect(jsonPath("$.numPieceIdentite").value("AB123456"))
                .andExpect(jsonPath("$.paysNom").value("Maroc"))
                .andExpect(jsonPath("$.paysCode").value("MA"))
                .andExpect(jsonPath("$.estSurListeSurveillance").value(false))
                .andExpect(jsonPath("$.notificationEmail").value(true))
                .andExpect(jsonPath("$.notificationSms").value(true))
                .andExpect(jsonPath("$.notificationPush").value(false));
    }

    @Test
    void getMyProfile_shouldReturn404WhenUserNotFound() throws Exception {
        when(clientProfileService.getMyProfile(99L))
                .thenThrow(new ResourceNotFoundException("Client introuvable"));

        mockMvc.perform(get("/api/v1/clients/profile")
                        .param("userId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Client introuvable"));
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /api/v1/clients/profile
    // ─────────────────────────────────────────────────────────────

    @Test
    void updateMyProfile_shouldReturn200WithUpdatedProfile() throws Exception {
        UpdateClientProfileRequestDto request = UpdateClientProfileRequestDto.builder()
                .nom("Benali")
                .prenom("Fatima")
                .telephone("+212600000021")
                .notificationEmail(false)
                .notificationSms(true)
                .notificationPush(true)
                .build();

        ClientProfileResponseDto updated = ClientProfileResponseDto.builder()
                .id(1L)
                .nom("Benali")
                .prenom("Fatima")
                .email("fatima.benali@okane.com")
                .telephone("+212600000021")
                .paysNom("Maroc")
                .paysCode("MA")
                .estSurListeSurveillance(false)
                .notificationEmail(false)
                .notificationSms(true)
                .notificationPush(true)
                .build();

        when(clientProfileService.updateMyProfile(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/clients/profile")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telephone").value("+212600000021"))
                .andExpect(jsonPath("$.notificationEmail").value(false))
                .andExpect(jsonPath("$.notificationSms").value(true))
                .andExpect(jsonPath("$.notificationPush").value(true));
    }

    @Test
    void updateMyProfile_shouldReturn400WhenNomIsBlank() throws Exception {
        UpdateClientProfileRequestDto request = UpdateClientProfileRequestDto.builder()
                .nom("")                      // @NotBlank violated
                .prenom("Fatima")
                .telephone("+212600000020")
                .build();

        mockMvc.perform(put("/api/v1/clients/profile")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateMyProfile_shouldReturn400WhenPrenomIsBlank() throws Exception {
        UpdateClientProfileRequestDto request = UpdateClientProfileRequestDto.builder()
                .nom("Benali")
                .prenom("")                   // @NotBlank violated
                .telephone("+212600000020")
                .build();

        mockMvc.perform(put("/api/v1/clients/profile")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMyProfile_shouldReturn400WhenTelephoneFormatIsInvalid() throws Exception {
        UpdateClientProfileRequestDto request = UpdateClientProfileRequestDto.builder()
                .nom("Benali")
                .prenom("Fatima")
                .telephone("abc123")          // @Pattern violated
                .build();

        mockMvc.perform(put("/api/v1/clients/profile")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMyProfile_shouldReturn400WhenTelephoneIsTooShort() throws Exception {
        UpdateClientProfileRequestDto request = UpdateClientProfileRequestDto.builder()
                .nom("Benali")
                .prenom("Fatima")
                .telephone("1234567")         // @Pattern min 8 chiffres violated
                .build();

        mockMvc.perform(put("/api/v1/clients/profile")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMyProfile_shouldReturn404WhenUserNotFound() throws Exception {
        UpdateClientProfileRequestDto request = UpdateClientProfileRequestDto.builder()
                .nom("Benali")
                .prenom("Fatima")
                .telephone("+212600000020")
                .build();

        when(clientProfileService.updateMyProfile(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Client introuvable"));

        mockMvc.perform(put("/api/v1/clients/profile")
                        .param("userId", "99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client introuvable"));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/clients/profile/activity
    // ─────────────────────────────────────────────────────────────

    @Test
    void getMyActivity_shouldReturn200WithPagedActivities() throws Exception {
        ClientActivityResponseDto activity = ClientActivityResponseDto.builder()
                .action("LOGIN")
                .details("Connexion réussie")
                .ipAddress("192.168.1.10")
                .timestamp(LocalDateTime.of(2026, 5, 30, 9, 0))
                .type("SECURITE")
                .build();

        PageResponseDto<ClientActivityResponseDto> page = new PageResponseDto<>(
                List.of(activity), 0, 20, 1L, 1, true
        );

        when(clientProfileService.getMyActivity(1L, 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/v1/clients/profile/activity")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("LOGIN"))
                .andExpect(jsonPath("$.content[0].details").value("Connexion réussie"))
                .andExpect(jsonPath("$.content[0].ipAddress").value("192.168.1.10"))
                .andExpect(jsonPath("$.content[0].type").value("SECURITE"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void getMyActivity_shouldReturn200WithEmptyPage() throws Exception {
        PageResponseDto<ClientActivityResponseDto> page = new PageResponseDto<>(
                List.of(), 0, 20, 0L, 0, true
        );

        when(clientProfileService.getMyActivity(1L, 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/v1/clients/profile/activity")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getMyActivity_shouldReturn200WithCustomPagination() throws Exception {
        PageResponseDto<ClientActivityResponseDto> page = new PageResponseDto<>(
                List.of(), 1, 10, 0L, 0, true
        );

        when(clientProfileService.getMyActivity(1L, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/v1/clients/profile/activity")
                        .param("userId", "1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getMyActivity_shouldReturn404WhenUserNotFound() throws Exception {
        when(clientProfileService.getMyActivity(99L, 0, 20))
                .thenThrow(new ResourceNotFoundException("Client introuvable"));

        mockMvc.perform(get("/api/v1/clients/profile/activity")
                        .param("userId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Client introuvable"));
    }
}