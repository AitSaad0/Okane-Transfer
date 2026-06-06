package com.okane.service.impl;

import com.okane.dto.converter.ClientConverter;
import com.okane.dto.requestDto.UpdateClientProfileRequestDto;
import com.okane.dto.responseDto.ClientActivityResponseDto;
import com.okane.dto.responseDto.ClientProfileResponseDto;
import com.okane.entity.Client;
import com.okane.entity.JournalAudit;
import com.okane.entity.Pays;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import com.okane.pagination.PageResponseDto;
import com.okane.repository.ClientRepository;
import com.okane.repository.JournalAuditRepository;
import com.okane.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientProfileServiceImplTest {

    @Mock private ClientRepository       clientRepository;
    @Mock private ClientConverter        clientConverter;
    @Mock private UserRepository         userRepository;
    @Mock private JournalAuditRepository journalAuditRepository;

    @InjectMocks
    private ClientProfileServiceImpl clientProfileService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private User   user;
    private Client client;
    private ClientProfileResponseDto profileDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("client@okane.ma")
                .password("encoded")
                .nom("Tazi")
                .prenom("Sara")
                .role(Role.CLIENT)
                .active(true)
                .deleted(false)
                .notificationEmail(true)
                .notificationSms(false)
                .notificationPush(false)
                .build();

        Pays pays = Pays.builder()
                .id(1L)
                .codeIso("MAR")
                .nom("Maroc")
                .build();

        client = Client.builder()
                .id(1L)
                .nom("Tazi")
                .prenom("Sara")
                .email("client@okane.ma")
                .telephone("0612345678")
                .numPieceIdentite("AB123456")
                .dateNaissance(LocalDate.of(1995, 6, 15))
                .estSurListeSurveillance(false)
                .deleted(false)
                .pays(pays)
                .user(user)
                .build();

        profileDto = ClientProfileResponseDto.builder()
                .id(1L)
                .nom("Tazi")
                .prenom("Sara")
                .email("client@okane.ma")
                .telephone("0612345678")
                .numPieceIdentite("AB123456")
                .dateNaissance(LocalDate.of(1995, 6, 15))
                .paysNom("Maroc")
                .paysCode("MAR")
                .estSurListeSurveillance(false)
                .notificationEmail(true)
                .notificationSms(false)
                .notificationPush(false)
                .build();
    }

    // ── getMyProfile ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getMyProfile()")
    class GetMyProfile {

        @Test
        @DisplayName("retourne le profil quand le client existe")
        void shouldReturnProfileDto() {
            when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(client));
            when(clientConverter.toProfileDto(client)).thenReturn(profileDto);

            ClientProfileResponseDto result = clientProfileService.getMyProfile(1L);

            assertNotNull(result);
            assertEquals(1L,               result.getId());
            assertEquals("Tazi",           result.getNom());
            assertEquals("client@okane.ma",result.getEmail());
            assertEquals("MAR",            result.getPaysCode());
            verify(clientRepository).findByUserId(1L);
            verify(clientConverter).toProfileDto(client);
        }

        @Test
        @DisplayName("lève EntityNotFoundException si aucun profil trouvé")
        void shouldThrowWhenClientNotFound() {
            when(clientRepository.findByUserId(99L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> clientProfileService.getMyProfile(99L));
            assertTrue(ex.getMessage().contains("Profil client introuvable"));
        }
    }

    // ── updateMyProfile ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateMyProfile()")
    class UpdateMyProfile {

        private UpdateClientProfileRequestDto request;

        @BeforeEach
        void init() {
            request = UpdateClientProfileRequestDto.builder()
                    .nom("NouveauNom")
                    .prenom("NouveauPrenom")
                    .telephone("0699999999")
                    .notificationEmail(false)
                    .notificationSms(true)
                    .notificationPush(true)
                    .build();
        }

        @Test
        @DisplayName("met à jour nom, prénom, téléphone et préférences notification")
        void shouldUpdateProfileAndNotifications() {
            when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(client));
            when(clientRepository.existsByTelephone("0699999999")).thenReturn(false);
            when(clientRepository.save(client)).thenReturn(client);
            when(userRepository.save(user)).thenReturn(user);
            when(clientConverter.toProfileDto(client)).thenReturn(profileDto);

            ClientProfileResponseDto result = clientProfileService.updateMyProfile(1L, request);

            assertNotNull(result);
            assertEquals("NouveauNom",    client.getNom());
            assertEquals("NouveauPrenom", client.getPrenom());
            assertEquals("0699999999",    client.getTelephone());
            assertFalse(user.getNotificationEmail());
            assertTrue(user.getNotificationSms());
            assertTrue(user.getNotificationPush());
            verify(clientRepository).save(client);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("ne vérifie pas l'unicité si le téléphone n'a pas changé")
        void shouldSkipPhoneCheckWhenUnchanged() {
            request.setTelephone("0612345678"); // même téléphone que le client
            when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);
            when(clientConverter.toProfileDto(client)).thenReturn(profileDto);

            clientProfileService.updateMyProfile(1L, request);

            verify(clientRepository, never()).existsByTelephone(any());
        }

        @Test
        @DisplayName("lève IllegalArgumentException si le nouveau téléphone est déjà utilisé")
        void shouldThrowWhenPhoneAlreadyUsed() {
            when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(client));
            when(clientRepository.existsByTelephone("0699999999")).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> clientProfileService.updateMyProfile(1L, request));
            assertTrue(ex.getMessage().contains("téléphone"));
        }

        @Test
        @DisplayName("lève EntityNotFoundException si le profil est introuvable")
        void shouldThrowWhenClientNotFound() {
            when(clientRepository.findByUserId(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> clientProfileService.updateMyProfile(99L, request));
            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("ne sauvegarde pas le User si user est null")
        void shouldNotSaveUserWhenNull() {
            client.setUser(null);
            request.setTelephone("0612345678"); // même tel, pas de vérif unicité
            when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);
            when(clientConverter.toProfileDto(client)).thenReturn(profileDto);

            clientProfileService.updateMyProfile(1L, request);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("ignore les préférences null sans écraser les valeurs existantes")
        void shouldIgnoreNullNotificationPreferences() {
            request.setNotificationEmail(null);
            request.setNotificationSms(null);
            request.setNotificationPush(null);
            request.setTelephone("0612345678");

            when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);
            when(userRepository.save(user)).thenReturn(user);
            when(clientConverter.toProfileDto(client)).thenReturn(profileDto);

            clientProfileService.updateMyProfile(1L, request);

            // valeurs initiales inchangées
            assertTrue(user.getNotificationEmail());
            assertFalse(user.getNotificationSms());
            assertFalse(user.getNotificationPush());
        }
    }

    // ── getMyActivity ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getMyActivity()")
    class GetMyActivity {

        private JournalAudit audit;

        @BeforeEach
        void init() {
            audit = JournalAudit.builder()
                    .id(1L)
                    .action("LOGIN")
                    .details("Connexion réussie")
                    .type("AUTH")
                    .timestamp(LocalDateTime.of(2024, 1, 15, 10, 30))
                    .ipAddress("192.168.1.1")
                    .utilisateur(user)
                    .build();
        }

        @Test
        @DisplayName("retourne une page d'activités triée par timestamp DESC")
        void shouldReturnPageOfActivities() {
            Page<JournalAudit> auditPage = new PageImpl<>(
                    List.of(audit), PageRequest.of(0, 10), 1);
            when(journalAuditRepository.findByUtilisateurId(eq(1L), any(Pageable.class)))
                    .thenReturn(auditPage);

            PageResponseDto<ClientActivityResponseDto> result =
                    clientProfileService.getMyActivity(1L, 0, 10);

            assertNotNull(result);
            assertEquals(1,  result.getContent().size());
            assertEquals(1L, result.getTotalElements());
            assertEquals(0,  result.getPage());
            assertTrue(result.isLast());

            ClientActivityResponseDto activity = result.getContent().get(0);
            assertEquals("LOGIN",            activity.getAction());
            assertEquals("AUTH",             activity.getType());
            assertEquals("192.168.1.1",      activity.getIpAddress());
            assertEquals("Connexion réussie",activity.getDetails());
            assertNotNull(activity.getTimestamp());
        }

        @Test
        @DisplayName("retourne page vide si aucune activité")
        void shouldReturnEmptyPage() {
            when(journalAuditRepository.findByUtilisateurId(eq(1L), any(Pageable.class)))
                    .thenReturn(Page.empty());

            PageResponseDto<ClientActivityResponseDto> result =
                    clientProfileService.getMyActivity(1L, 0, 10);

            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertEquals(0L, result.getTotalElements());
        }

        @Test
        @DisplayName("applique bien la pagination — page 1 size 5")
        void shouldApplyPagination() {
            Page<JournalAudit> page = new PageImpl<>(
                    List.of(audit), PageRequest.of(1, 5), 11); // 11 > (1+1)*5 → isLast=false
            when(journalAuditRepository.findByUtilisateurId(eq(1L), any(Pageable.class)))
                    .thenReturn(page);

            PageResponseDto<ClientActivityResponseDto> result =
                    clientProfileService.getMyActivity(1L, 1, 5);

            assertEquals(1,   result.getPage());
            assertEquals(5,   result.getSize());
            assertEquals(11L, result.getTotalElements());
            assertFalse(result.isLast());
        }

        @Test
        @DisplayName("le tri DESC par timestamp est bien passé au repository")
        void shouldSortByTimestampDesc() {
            when(journalAuditRepository.findByUtilisateurId(eq(1L), any(Pageable.class)))
                    .thenReturn(Page.empty());

            clientProfileService.getMyActivity(1L, 0, 10);

            verify(journalAuditRepository).findByUtilisateurId(eq(1L),
                    argThat(p -> p.getSort().getOrderFor("timestamp") != null
                            && p.getSort().getOrderFor("timestamp").getDirection()
                            == Sort.Direction.DESC));
        }
    }
}