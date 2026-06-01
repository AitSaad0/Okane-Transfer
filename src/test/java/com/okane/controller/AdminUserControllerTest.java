package com.okane.controller;

import static org.mockito.Mockito.isNull;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okane.dto.requestDto.CreateUserRequestDto;
import com.okane.dto.requestDto.UpdateUserRequestDto;
import com.okane.dto.requestDto.UpdateUserStatusRequestDto;
import com.okane.dto.responseDto.UserResponseDTO;
import com.okane.entity.enums.Role;
import com.okane.exception.GlobalExceptionHandler;
import com.okane.exception.ResourceNotFoundException;
import com.okane.pagination.PageResponseDto;
import com.okane.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    private UserResponseDTO sampleUser;

    @BeforeEach
    void setUp() {
        jakarta.validation.Validator validator = jakarta.validation.Validation
                .byDefaultProvider()
                .configure()
                .messageInterpolator(new org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();

        org.springframework.validation.beanvalidation.SpringValidatorAdapter springValidator =
                new org.springframework.validation.beanvalidation.SpringValidatorAdapter(validator);

        mockMvc = MockMvcBuilders
                .standaloneSetup(adminUserController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(springValidator)
                .build();

        sampleUser = UserResponseDTO.builder()
                .id(1L)
                .email("agent@okane.com")
                .nom("Dupont")
                .prenom("Marie")
                .telephone("+212600000001")
                .role(Role.AGENT)
                .active(true)
                .agenceId(10L)
                .agenceNom("Agence Casablanca Centre")
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/admin/users
    // ─────────────────────────────────────────────────────────────

    @Test
    void getAllUsers_shouldReturn200WithPagedResult() throws Exception {
        PageResponseDto<UserResponseDTO> page = new PageResponseDto<>(
                List.of(sampleUser), 0, 20, 1L, 1, true
        );

        when(userService.getAllUsers(null, null, null, 0, 20, "id")).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].email").value("agent@okane.com"))
                .andExpect(jsonPath("$.content[0].nom").value("Dupont"))
                .andExpect(jsonPath("$.content[0].role").value("AGENT"))
                .andExpect(jsonPath("$.content[0].active").value(true));
    }

    @Test
    void getAllUsers_shouldReturn200FilteredByRole() throws Exception {
        PageResponseDto<UserResponseDTO> page = new PageResponseDto<>(
                List.of(sampleUser), 0, 20, 1L, 1, true
        );

        when(userService.getAllUsers(eq(Role.AGENT), isNull(), isNull(), eq(0), eq(20), eq("id")))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("role", "AGENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].role").value("AGENT"));
    }

    @Test
    void getAllUsers_shouldReturn200FilteredByActiveAndAgence() throws Exception {
        PageResponseDto<UserResponseDTO> page = new PageResponseDto<>(
                List.of(sampleUser), 0, 20, 1L, 1, true
        );

        when(userService.getAllUsers(isNull(), eq(true), eq(10L), eq(0), eq(20), eq("id")))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("active", "true")
                        .param("agenceId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].agenceId").value(10L))
                .andExpect(jsonPath("$.content[0].active").value(true));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/admin/users/{id}
    // ─────────────────────────────────────────────────────────────

    @Test
    void getUserById_shouldReturn200WithAllFields() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleUser);

        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("agent@okane.com"))
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.prenom").value("Marie"))
                .andExpect(jsonPath("$.telephone").value("+212600000001"))
                .andExpect(jsonPath("$.role").value("AGENT"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.agenceId").value(10L))
                .andExpect(jsonPath("$.agenceNom").value("Agence Casablanca Centre"));
    }

    @Test
    void getUserById_shouldReturn404WhenNotFound() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("Utilisateur introuvable"));

        mockMvc.perform(get("/api/v1/admin/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Utilisateur introuvable"));
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/v1/admin/users
    // ─────────────────────────────────────────────────────────────

    @Test
    void createUser_shouldReturn201WithCreatedUser() throws Exception {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "agent@okane.com", "password123", "Dupont", "Marie",
                "+212600000001", Role.AGENT, 10L
        );

        when(userService.createUser(any())).thenReturn(sampleUser);

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("agent@okane.com"))
                .andExpect(jsonPath("$.role").value("AGENT"));
    }

    @Test
    void createUser_shouldReturn400WhenEmailIsInvalid() throws Exception {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "not-an-email", "password123", "Dupont", "Marie",
                null, Role.AGENT, null
        );

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createUser_shouldReturn400WhenPasswordIsTooShort() throws Exception {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "agent@okane.com", "short", "Dupont", "Marie",
                null, Role.AGENT, null  // @Size(min=8) violated
        );

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn400WhenRoleIsNull() throws Exception {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "agent@okane.com", "password123", "Dupont", "Marie",
                null, null, null  // @NotNull violated
        );

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn400WhenNomIsBlank() throws Exception {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "agent@okane.com", "password123", "", "Marie",
                null, Role.AGENT, null  // @NotBlank violated
        );

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /api/v1/admin/users/{id}
    // ─────────────────────────────────────────────────────────────

    @Test
    void updateUser_shouldReturn200WithUpdatedUser() throws Exception {
        UpdateUserRequestDto request = new UpdateUserRequestDto(
                "Martin", "Sophie", "+212600000002", 10L
        );

        UserResponseDTO updated = UserResponseDTO.builder()
                .id(1L)
                .email("agent@okane.com")
                .nom("Martin")
                .prenom("Sophie")
                .telephone("+212600000002")
                .role(Role.AGENT)
                .active(true)
                .agenceId(10L)
                .agenceNom("Agence Casablanca Centre")
                .build();

        when(userService.updateUser(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Martin"))
                .andExpect(jsonPath("$.prenom").value("Sophie"))
                .andExpect(jsonPath("$.telephone").value("+212600000002"));
    }

    @Test
    void updateUser_shouldReturn400WhenNomIsBlank() throws Exception {
        UpdateUserRequestDto request = new UpdateUserRequestDto(
                "", "Sophie", null, null  // @NotBlank violated
        );

        mockMvc.perform(put("/api/v1/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_shouldReturn404WhenNotFound() throws Exception {
        UpdateUserRequestDto request = new UpdateUserRequestDto(
                "Martin", "Sophie", null, null
        );

        when(userService.updateUser(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Utilisateur introuvable"));

        mockMvc.perform(put("/api/v1/admin/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Utilisateur introuvable"));
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH /api/v1/admin/users/{id}/status
    // ─────────────────────────────────────────────────────────────

    @Test
    void updateUserStatus_shouldReturn200WhenDeactivated() throws Exception {
        UpdateUserStatusRequestDto request = new UpdateUserStatusRequestDto(false);

        UserResponseDTO deactivated = UserResponseDTO.builder()
                .id(1L)
                .email("agent@okane.com")
                .nom("Dupont")
                .prenom("Marie")
                .role(Role.AGENT)
                .active(false)
                .build();

        when(userService.updateUserStatus(eq(1L), any())).thenReturn(deactivated);

        mockMvc.perform(patch("/api/v1/admin/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateUserStatus_shouldReturn200WhenReactivated() throws Exception {
        UpdateUserStatusRequestDto request = new UpdateUserStatusRequestDto(true);

        when(userService.updateUserStatus(eq(1L), any())).thenReturn(sampleUser);

        mockMvc.perform(patch("/api/v1/admin/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void updateUserStatus_shouldReturn400WhenActiveIsNull() throws Exception {
        String body = "{\"active\": null}";  // @NotNull violated

        mockMvc.perform(patch("/api/v1/admin/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserStatus_shouldReturn404WhenNotFound() throws Exception {
        UpdateUserStatusRequestDto request = new UpdateUserStatusRequestDto(false);

        when(userService.updateUserStatus(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Utilisateur introuvable"));

        mockMvc.perform(patch("/api/v1/admin/users/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Utilisateur introuvable"));
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE /api/v1/admin/users/{id}
    // ─────────────────────────────────────────────────────────────

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Utilisateur introuvable"))
                .when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/v1/admin/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Utilisateur introuvable"));
    }
}