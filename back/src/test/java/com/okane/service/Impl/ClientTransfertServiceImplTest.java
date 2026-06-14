package com.okane.service.Impl;

import com.okane.dto.requestDto.ForceCancelRequestDTO;
import com.okane.dto.responseDto.TransfertResponseDTO;
import com.okane.entity.*;
import com.okane.entity.enums.StatutTransfert;
import com.okane.exception.BadRequestException;
import com.okane.exception.ResourceNotFoundException;
import com.okane.exception.UnauthorizedException;
import com.okane.pagination.PageResponseDto;
import com.okane.repository.TransfertRepository;
import com.okane.repository.UserRepository;
import com.okane.service.impl.ClientTransfertServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientTransfertServiceImplTest {

    @Mock private TransfertRepository transfertRepository;
    @Mock private UserRepository      userRepository;

    @InjectMocks
    private ClientTransfertServiceImpl service;

    // ── fixtures ──────────────────────────────────────────────────

    private Client    expediteur;
    private Client    beneficiaire;
    private User      userClient;
    private Transfert transfert;
    private Corridor  corridor;
    private Agence    agenceEnvoi;
    private User      agentEnvoi;

    @BeforeEach
    void setUp() {
        expediteur = new Client();
        expediteur.setId(1L);
        expediteur.setNom("Martin");
        expediteur.setPrenom("Alice");
        expediteur.setTelephone("0612345678");
        expediteur.setEmail("alice@mail.ma");

        beneficiaire = new Client();
        beneficiaire.setId(2L);
        beneficiaire.setNom("Lemaire");
        beneficiaire.setPrenom("Bob");
        beneficiaire.setTelephone("0698765432");
        beneficiaire.setEmail("bob@mail.ma");

        userClient = new User();
        userClient.setId(10L);
        userClient.setEmail("alice@mail.ma");
        userClient.setClient(expediteur);

        Devise deviseSource = new Devise();
        deviseSource.setCode("MAD");

        Devise deviseDestination = new Devise();
        deviseDestination.setCode("EUR");

        Pays paysOrigine = new Pays();
        paysOrigine.setNom("Maroc");

        Pays paysDestination = new Pays();
        paysDestination.setNom("France");

        corridor = new Corridor();
        corridor.setId(5L);
        corridor.setDeviseSource(deviseSource);
        corridor.setDeviseDestination(deviseDestination);
        corridor.setPaysOrigine(paysOrigine);
        corridor.setPaysDestination(paysDestination);
        corridor.setTauxChange(new BigDecimal("10.80"));

        agenceEnvoi = new Agence();
        agenceEnvoi.setId(20L);
        agenceEnvoi.setNom("Agence Casa");

        agentEnvoi = new User();
        agentEnvoi.setId(30L);
        agentEnvoi.setNom("Dupont");
        agentEnvoi.setPrenom("Jean");

        transfert = new Transfert();
        transfert.setId(100L);
        transfert.setCodeRetrait("XYZ789");
        transfert.setStatut(StatutTransfert.EN_ATTENTE);
        transfert.setMontantEnvoye(new BigDecimal("500.00"));
        transfert.setMontantNet(new BigDecimal("490.00"));
        transfert.setFrais(new BigDecimal("10.00"));
        transfert.setDateCreation(LocalDateTime.now().minusDays(1));
        transfert.setExpediteur(expediteur);
        transfert.setBeneficiaire(beneficiaire);
        transfert.setCorridor(corridor);
        transfert.setAgenceEnvoi(agenceEnvoi);
        transfert.setAgentEnvoi(agentEnvoi);
        transfert.setEstSuspect(false);
    }

    // ─────────────────────────────────────────────────────────────
    // getTransfertsClient()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getTransfertsClient_shouldReturnPagedResultsForClient() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transfert> page = new PageImpl<>(List.of(transfert), pageable, 1L);

        when(userRepository.findByEmail("alice@mail.ma")).thenReturn(Optional.of(userClient));
        when(transfertRepository.findByClient(expediteur, pageable)).thenReturn(page);

        PageResponseDto<TransfertResponseDTO> result =
                service.getTransfertsClient("alice@mail.ma", pageable);

        assertNotNull(result);
        assertEquals(1,    result.getContent().size());
        assertEquals(1L,   result.getTotalElements());
        assertEquals(0,    result.getPage());
        assertEquals(10,   result.getSize());
        assertTrue(result.isLast());
        verify(transfertRepository).findByClient(expediteur, pageable);
    }

    @Test
    void getTransfertsClient_shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("inconnu@mail.ma")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getTransfertsClient("inconnu@mail.ma", PageRequest.of(0, 10)));
    }

    @Test
    void getTransfertsClient_shouldThrowWhenUserHasNoClientProfile() {
        User userSansClient = new User();
        userSansClient.setEmail("noClient@mail.ma");
        userSansClient.setClient(null);

        when(userRepository.findByEmail("noClient@mail.ma")).thenReturn(Optional.of(userSansClient));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.getTransfertsClient("noClient@mail.ma", PageRequest.of(0, 10)));

        assertTrue(ex.getMessage().contains("Aucun profil client"));
    }

    // ─────────────────────────────────────────────────────────────
    // getTransfertClientById()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getTransfertClientById_shouldReturnDtoWhenOwnerIsExpediteur() {
        when(userRepository.findByEmail("alice@mail.ma")).thenReturn(Optional.of(userClient));
        when(transfertRepository.findById(100L)).thenReturn(Optional.of(transfert));

        TransfertResponseDTO result = service.getTransfertClientById(100L, "alice@mail.ma");

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Martin", result.getExpediteurNom());
        assertEquals("alice@mail.ma", result.getExpediteurEmail());
    }

    @Test
    void getTransfertClientById_shouldReturnDtoWhenOwnerIsBeneficiaire() {
        User userBenef = new User();
        userBenef.setEmail("bob@mail.ma");
        userBenef.setClient(beneficiaire);

        when(userRepository.findByEmail("bob@mail.ma")).thenReturn(Optional.of(userBenef));
        when(transfertRepository.findById(100L)).thenReturn(Optional.of(transfert));

        TransfertResponseDTO result = service.getTransfertClientById(100L, "bob@mail.ma");

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    void getTransfertClientById_shouldThrowUnauthorizedWhenNotOwner() {
        Client stranger = new Client();
        stranger.setId(99L);

        User stranger_user = new User();
        stranger_user.setEmail("stranger@mail.ma");
        stranger_user.setClient(stranger);

        when(userRepository.findByEmail("stranger@mail.ma")).thenReturn(Optional.of(stranger_user));
        when(transfertRepository.findById(100L)).thenReturn(Optional.of(transfert));

        assertThrows(UnauthorizedException.class,
                () -> service.getTransfertClientById(100L, "stranger@mail.ma"));
    }

    @Test
    void getTransfertClientById_shouldThrowWhenTransfertNotFound() {
        when(userRepository.findByEmail("alice@mail.ma")).thenReturn(Optional.of(userClient));
        when(transfertRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getTransfertClientById(999L, "alice@mail.ma"));
    }

    // ─────────────────────────────────────────────────────────────
    // trackTransfert()
    // ─────────────────────────────────────────────────────────────

    @Test
    void trackTransfert_shouldReturnPublicDtoWithMaskedBeneficiaire() {
        transfert.setStatut(StatutTransfert.EN_ATTENTE);

        when(transfertRepository.findByCodeRetrait("XYZ789")).thenReturn(Optional.of(transfert));

        TransfertResponseDTO result = service.trackTransfert("XYZ789");

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("XYZ789", result.getCodeRetrait());
        assertEquals(StatutTransfert.EN_ATTENTE, result.getStatut());
        // Données sensibles masquées
        assertNull(result.getExpediteurNom());
        assertEquals("L***", result.getBeneficiaireNom());
        assertEquals("B***", result.getBeneficiairePrenom());
        // Expiration = dateCréation + 30 jours
        assertEquals(transfert.getDateCreation().plusDays(30), result.getDateExpiration());
    }

    @Test
    void trackTransfert_shouldReturnDeviseInfoWithoutSensitiveData() {
        when(transfertRepository.findByCodeRetrait("XYZ789")).thenReturn(Optional.of(transfert));

        TransfertResponseDTO result = service.trackTransfert("XYZ789");

        assertEquals("MAD", result.getDeviseSource());
        assertEquals("EUR", result.getDeviseDestination());
        assertEquals("Maroc → France", result.getCorridorDescription());
        assertNull(result.getExpediteurTelephone());
    }

    @Test
    void trackTransfert_shouldThrowWhenCodeRetraitUnknown() {
        when(transfertRepository.findByCodeRetrait("INVALID")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.trackTransfert("INVALID"));

        assertTrue(ex.getMessage().contains("INVALID"));
    }

    // ─────────────────────────────────────────────────────────────
    // getAllTransfertsAdmin()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getAllTransfertsAdmin_shouldReturnAllWhenNoFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Transfert> page = new PageImpl<>(List.of(transfert));

        when(transfertRepository.findAllWithFilters(null, null, null, null, null, pageable))
                .thenReturn(page);

        PageResponseDto<TransfertResponseDTO> result =
                service.getAllTransfertsAdmin(null, null, null, null, null, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAllTransfertsAdmin_shouldFilterByStatutEnum() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transfert> page = new PageImpl<>(List.of(transfert));

        when(transfertRepository.findAllWithFilters(
                eq(StatutTransfert.EN_ATTENTE), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(page);

        PageResponseDto<TransfertResponseDTO> result =
                service.getAllTransfertsAdmin("EN_ATTENTE", null, null, null, null, pageable);

        assertEquals(1, result.getContent().size());
        verify(transfertRepository).findAllWithFilters(
                eq(StatutTransfert.EN_ATTENTE), any(), any(), any(), any(), eq(pageable));
    }

    @Test
    void getAllTransfertsAdmin_shouldThrowWhenStatutInvalid() {
        assertThrows(BadRequestException.class,
                () -> service.getAllTransfertsAdmin("MAUVAIS_STATUT", null, null, null, null,
                        PageRequest.of(0, 10)));
    }

    @Test
    void getAllTransfertsAdmin_shouldParseAndPassDateFilters() {
        String debut = "2025-01-01T00:00:00";
        String fin   = "2025-01-31T23:59:59";
        Pageable pageable = PageRequest.of(0, 10);

        when(transfertRepository.findAllWithFilters(any(), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getAllTransfertsAdmin(null, null, null, debut, fin, pageable);

        verify(transfertRepository).findAllWithFilters(
                isNull(),
                isNull(),
                isNull(),
                eq(java.time.LocalDateTime.parse(debut)),
                eq(java.time.LocalDateTime.parse(fin)),
                eq(pageable));
    }

    // ─────────────────────────────────────────────────────────────
    // getTransfertAdminById()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getTransfertAdminById_shouldReturnFullDto() {
        when(transfertRepository.findById(100L)).thenReturn(Optional.of(transfert));

        TransfertResponseDTO result = service.getTransfertAdminById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        // Données d'agent incluses
        assertEquals(30L, result.getAgentId());
        assertEquals("Dupont", result.getAgentNom());
        // Corridor
        assertEquals("MAD", result.getDeviseSource());
        assertEquals("EUR", result.getDeviseDestination());
        assertEquals(new BigDecimal("10.80"), result.getTauxChange());
        // Flag suspect
        assertFalse(result.isFlagged());
    }

    @Test
    void getTransfertAdminById_shouldThrowWhenNotFound() {
        when(transfertRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getTransfertAdminById(999L));
    }

    // ─────────────────────────────────────────────────────────────
    // forceCancelTransfert()
    // ─────────────────────────────────────────────────────────────

    @Test
    void forceCancelTransfert_shouldSetStatutAnnule() {
        transfert.setStatut(StatutTransfert.EN_ATTENTE);
        when(transfertRepository.findById(100L)).thenReturn(Optional.of(transfert));
        when(transfertRepository.save(any(Transfert.class))).thenAnswer(inv -> inv.getArgument(0));

        service.forceCancelTransfert(100L, new ForceCancelRequestDTO(), "admin@okane.ma");

        assertEquals(StatutTransfert.ANNULE, transfert.getStatut());
        verify(transfertRepository).save(transfert);
    }

    @Test
    void forceCancelTransfert_shouldThrowWhenAlreadyPaid() {
        transfert.setStatut(StatutTransfert.PAYE);
        when(transfertRepository.findById(100L)).thenReturn(Optional.of(transfert));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.forceCancelTransfert(100L, new ForceCancelRequestDTO(), "admin@okane.ma"));

        assertTrue(ex.getMessage().contains("déjà payé"));
        verify(transfertRepository, never()).save(any());
    }

    @Test
    void forceCancelTransfert_shouldThrowWhenAlreadyCancelled() {
        transfert.setStatut(StatutTransfert.ANNULE);
        when(transfertRepository.findById(100L)).thenReturn(Optional.of(transfert));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.forceCancelTransfert(100L, new ForceCancelRequestDTO(), "admin@okane.ma"));

        assertTrue(ex.getMessage().contains("déjà annulé"));
        verify(transfertRepository, never()).save(any());
    }

    @Test
    void forceCancelTransfert_shouldThrowWhenTransfertNotFound() {
        when(transfertRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.forceCancelTransfert(999L, new ForceCancelRequestDTO(), "admin@okane.ma"));
    }

    // ─────────────────────────────────────────────────────────────
    // getTransfertsManager()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getTransfertsManager_shouldReturnTransfertsForManagerAgence() {
        Agence agence = new Agence();
        agence.setId(20L);

        User manager = new User();
        manager.setEmail("manager@okane.ma");
        manager.setAgence(agence);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Transfert> page = new PageImpl<>(List.of(transfert));

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transfertRepository.findByAgence(20L, null, pageable)).thenReturn(page);

        PageResponseDto<TransfertResponseDTO> result =
                service.getTransfertsManager("manager@okane.ma", null, pageable);

        assertEquals(1, result.getContent().size());
        verify(transfertRepository).findByAgence(20L, null, pageable);
    }

    @Test
    void getTransfertsManager_shouldThrowWhenManagerHasNoAgence() {
        User managerSansAgence = new User();
        managerSansAgence.setEmail("manager@okane.ma");
        managerSansAgence.setAgence(null);

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(managerSansAgence));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.getTransfertsManager("manager@okane.ma", null, PageRequest.of(0, 10)));

        assertTrue(ex.getMessage().contains("aucune agence"));
    }

    @Test
    void getTransfertsManager_shouldThrowWhenStatutInvalid() {
        Agence agence = new Agence();
        agence.setId(20L);

        User manager = new User();
        manager.setEmail("manager@okane.ma");
        manager.setAgence(agence);

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));

        assertThrows(BadRequestException.class,
                () -> service.getTransfertsManager("manager@okane.ma", "INVALID", PageRequest.of(0, 10)));
    }
}