package com.okane.service.impl;

import com.okane.dto.converter.UserConverter;
import com.okane.dto.requestDto.CreateUserRequestDto;
import com.okane.dto.requestDto.UpdateUserRequestDto;
import com.okane.dto.requestDto.UpdateUserStatusRequestDto;
import com.okane.dto.responseDto.UserResponseDTO;
import com.okane.entity.Agence;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import com.okane.entity.enums.StatutAgence;
import com.okane.pagination.PageResponseDto;
import com.okane.repository.AgenceRepository;
import com.okane.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository   userRepository;
    @Mock private AgenceRepository agenceRepository;
    @Mock private UserConverter    userConverter;
    @Mock private PasswordEncoder  passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Agence agence;
    private User   user;
    private UserResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        agence = Agence.builder()
                .id(1L)
                .nom("Agence Casablanca")
                .adresse("123 Rue Hassan II")
                .plafondJournalier(BigDecimal.valueOf(50000))
                .statut(StatutAgence.ACTIVE)
                .build();

        user = User.builder()
                .id(1L)
                .email("ali@okane.ma")
                .password("encoded_pass")
                .nom("Alaoui")
                .prenom("Ali")
                .telephone("0612345678")
                .role(Role.AGENT)
                .active(true)
                .deleted(false)
                .agence(agence)
                .build();

        responseDTO = UserResponseDTO.builder()
                .id(1L)
                .email("ali@okane.ma")
                .nom("Alaoui")
                .prenom("Ali")
                .telephone("0612345678")
                .role(Role.AGENT)
                .active(true)
                .agenceId(1L)
                .agenceNom("Agence Casablanca")
                .build();
    }

    // ── getAllUsers ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("retourne une page de users sans filtres")
        void shouldReturnPageOfUsers() {
            Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
            when(userRepository.findAllWithFilters(isNull(), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(page);
            when(userConverter.toResponseDto(user)).thenReturn(responseDTO);

            PageResponseDto<UserResponseDTO> result =
                    userService.getAllUsers(null, null, null, 0, 10, "id");

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(1L, result.getTotalElements());
            assertEquals(0, result.getPage());
            assertTrue(result.isLast());
        }

        @Test
        @DisplayName("filtre par role, active et agenceId")
        void shouldFilterByRoleActiveAndAgence() {
            Page<User> page = new PageImpl<>(List.of(user));
            when(userRepository.findAllWithFilters(eq(Role.AGENT), eq(true), eq(1L), any(Pageable.class)))
                    .thenReturn(page);
            when(userConverter.toResponseDto(user)).thenReturn(responseDTO);

            PageResponseDto<UserResponseDTO> result =
                    userService.getAllUsers(Role.AGENT, true, 1L, 0, 10, "nom");

            assertEquals(1, result.getContent().size());
            verify(userRepository).findAllWithFilters(eq(Role.AGENT), eq(true), eq(1L), any(Pageable.class));
        }

        @Test
        @DisplayName("retourne page vide quand aucun user ne correspond")
        void shouldReturnEmptyPage() {
            when(userRepository.findAllWithFilters(any(), any(), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            PageResponseDto<UserResponseDTO> result =
                    userService.getAllUsers(Role.ADMIN, false, 99L, 0, 10, "id");

            assertTrue(result.getContent().isEmpty());
            assertEquals(0L, result.getTotalElements());
        }
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("retourne le DTO quand l'utilisateur existe")
        void shouldReturnUserDto() {
            when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
            when(userConverter.toResponseDto(user)).thenReturn(responseDTO);

            UserResponseDTO result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("ali@okane.ma", result.getEmail());
        }

        @Test
        @DisplayName("lève EntityNotFoundException si l'id est inconnu")
        void shouldThrowWhenNotFound() {
            when(userRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> userService.getUserById(99L));
            assertTrue(ex.getMessage().contains("99"));
        }

        @Test
        @DisplayName("lève EntityNotFoundException si l'utilisateur est soft-deleted")
        void shouldThrowWhenDeleted() {
            when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> userService.getUserById(1L));
        }
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        private CreateUserRequestDto request;

        @BeforeEach
        void init() {
            request = new CreateUserRequestDto(
                    "nouveau@okane.ma", "Password1!", "Benali", "Sara",
                    "0699999999", Role.AGENT, 1L
            );
        }

        @Test
        @DisplayName("crée et retourne un nouvel utilisateur avec agence")
        void shouldCreateUserWithAgence() {
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
            when(passwordEncoder.encode("Password1!")).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userConverter.toResponseDto(user)).thenReturn(responseDTO);

            UserResponseDTO result = userService.createUser(request);

            assertNotNull(result);
            verify(passwordEncoder).encode("Password1!");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("crée un utilisateur sans agence quand agenceId est null")
        void shouldCreateUserWithoutAgence() {
            request.setAgenceId(null);
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userConverter.toResponseDto(user)).thenReturn(responseDTO);

            userService.createUser(request);

            verify(agenceRepository, never()).findById(any());
        }

        @Test
        @DisplayName("lève IllegalArgumentException si l'email existe déjà")
        void shouldThrowWhenEmailAlreadyExists() {
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.createUser(request));
            assertTrue(ex.getMessage().toLowerCase().contains("email"));
        }

        @Test
        @DisplayName("lève EntityNotFoundException si agenceId est introuvable")
        void shouldThrowWhenAgenceNotFound() {
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(agenceRepository.findById(1L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> userService.createUser(request));
            assertTrue(ex.getMessage().contains("Agence"));
        }
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateUser()")
    class UpdateUser {

        private UpdateUserRequestDto request;

        @BeforeEach
        void init() {
            request = new UpdateUserRequestDto("NouveauNom", "NouveauPrenom", "0611000000", 1L);
        }

        @Test
        @DisplayName("met à jour nom, prénom, téléphone et agence")
        void shouldUpdateUserFields() {
            when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
            when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
            when(userRepository.save(user)).thenReturn(user);
            when(userConverter.toResponseDto(user)).thenReturn(responseDTO);

            UserResponseDTO result = userService.updateUser(1L, request);

            assertNotNull(result);
            assertEquals("NouveauNom", user.getNom());
            assertEquals("NouveauPrenom", user.getPrenom());
            assertEquals("0611000000", user.getTelephone());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("lève EntityNotFoundException si l'utilisateur est introuvable")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByIdAndDeletedFalse(42L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> userService.updateUser(42L, request));
            assertTrue(ex.getMessage().contains("42"));
        }

        @Test
        @DisplayName("met à jour sans agence quand agenceId est null")
        void shouldUpdateWithoutAgence() {
            request.setAgenceId(null);
            when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userConverter.toResponseDto(user)).thenReturn(responseDTO);

            userService.updateUser(1L, request);

            verify(agenceRepository, never()).findById(any());
            assertNull(user.getAgence());
        }
    }

    // ── updateUserStatus ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateUserStatus()")
    class UpdateUserStatus {

        @Test
        @DisplayName("active un utilisateur inactif")
        void shouldActivateUser() {
            user.setActive(false);
            when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userConverter.toResponseDto(user)).thenReturn(responseDTO);

            userService.updateUserStatus(1L, new UpdateUserStatusRequestDto(true));

            assertTrue(user.getActive());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("désactive un utilisateur actif")
        void shouldDeactivateUser() {
            when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userConverter.toResponseDto(user)).thenReturn(responseDTO);

            userService.updateUserStatus(1L, new UpdateUserStatusRequestDto(false));

            assertFalse(user.getActive());
        }

        @Test
        @DisplayName("lève EntityNotFoundException si user introuvable")
        void shouldThrowWhenNotFound() {
            when(userRepository.findByIdAndDeletedFalse(5L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> userService.updateUserStatus(5L, new UpdateUserStatusRequestDto(true)));
            assertTrue(ex.getMessage().contains("5"));
        }
    }

    // ── deleteUser ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("anonymise les données personnelles et marque deleted=true")
        void shouldAnonymizeAndSoftDelete() {
            when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));

            userService.deleteUser(1L);

            assertTrue(user.getDeleted());
            assertFalse(user.getActive());
            assertEquals("ANONYMIZED", user.getNom());
            assertEquals("ANONYMIZED", user.getPrenom());
            assertTrue(user.getEmail().startsWith("deleted_"));
            assertTrue(user.getEmail().endsWith("@anonymized.local"));
            assertNull(user.getTelephone());
            assertNull(user.getTwoFactorSecret());
            assertNotNull(user.getDeletedAt());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("lève EntityNotFoundException si l'utilisateur est introuvable")
        void shouldThrowWhenNotFound() {
            when(userRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> userService.deleteUser(99L));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("chaque suppression génère un token d'anonymisation unique")
        void shouldGenerateUniqueAnonymToken() {
            User user2 = User.builder()
                    .id(2L).email("b@b.com").nom("X").prenom("Y")
                    .active(true).deleted(false).role(Role.AGENT).password("p")
                    .build();

            when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
            when(userRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(user2));

            userService.deleteUser(1L);
            userService.deleteUser(2L);

            assertNotEquals(user.getEmail(), user2.getEmail());
        }
    }
}