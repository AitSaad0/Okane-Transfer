package com.okane.service.Impl;

import com.okane.dto.requestDto.ClotureCaisseRequestDTO;
import com.okane.dto.requestDto.EcartCaisseRequestDTO;
import com.okane.dto.responseDto.CaisseResponseDTO;
import com.okane.entity.*;
import com.okane.entity.enums.StatutCaisse;
import com.okane.exception.BadRequestException;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.CaisseRepository;
import com.okane.repository.TransfertRepository;
import com.okane.repository.UserRepository;
import com.okane.service.impl.CaisseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaisseServiceImplTest {

    @Mock private CaisseRepository    caisseRepository;
    @Mock private UserRepository      userRepository;
    @Mock private TransfertRepository transfertRepository;

    @InjectMocks
    private CaisseServiceImpl caisseService;

    // ── fixtures ──────────────────────────────────────────────────

    private User    agent;
    private Agence  agence;
    private Caisse  caisse;

    @BeforeEach
    void setUp() {
        agence = new Agence();
        agence.setId(1L);
        agence.setNom("Agence Casa");

        agent = new User();
        agent.setId(10L);
        agent.setEmail("agent@okane.ma");
        agent.setNom("Dupont");
        agent.setPrenom("Jean");
        agent.setAgence(agence);

        caisse = new Caisse();
        caisse.setId(100L);
        caisse.setAgent(agent);
        caisse.setStatut(StatutCaisse.OUVERTE);
        caisse.setDateCaisse(LocalDate.now());
        caisse.setSoldeOuverture(new BigDecimal("5000.00"));
        caisse.setSoldeCourant(new BigDecimal("6500.00"));
        caisse.setTotalEncaissements(new BigDecimal("2000.00"));
        caisse.setTotalDecaissements(new BigDecimal("500.00"));
        caisse.setDateOuverture(LocalDateTime.now().minusHours(3));
    }

    // ─────────────────────────────────────────────────────────────
    // getCaisseCourante()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getCaisseCourante_shouldReturnDtoWhenCaisseOuverte() {
        when(userRepository.findByEmail("agent@okane.ma")).thenReturn(Optional.of(agent));
        when(caisseRepository.findByAgentAndDateCaisseAndStatut(agent, LocalDate.now(), StatutCaisse.OUVERTE))
                .thenReturn(Optional.of(caisse));

        CaisseResponseDTO result = caisseService.getCaisseCourante("agent@okane.ma");

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(StatutCaisse.OUVERTE, result.getStatut());
        assertEquals("Dupont",  result.getAgentNom());
        assertEquals("Jean",    result.getAgentPrenom());
        assertEquals("Agence Casa", result.getAgenceNom());
        // Sans opérations (avecOperations = false)
        assertNull(result.getOperations());
    }

    @Test
    void getCaisseCourante_shouldThrowWhenAgentNotFound() {
        when(userRepository.findByEmail("inconnu@okane.ma")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> caisseService.getCaisseCourante("inconnu@okane.ma"));

        verify(caisseRepository, never()).findByAgentAndDateCaisseAndStatut(any(), any(), any());
    }

    @Test
    void getCaisseCourante_shouldThrowWhenNoCaisseOuverte() {
        when(userRepository.findByEmail("agent@okane.ma")).thenReturn(Optional.of(agent));
        when(caisseRepository.findByAgentAndDateCaisseAndStatut(any(), any(), any()))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> caisseService.getCaisseCourante("agent@okane.ma"));

        assertTrue(ex.getMessage().contains("Aucune caisse ouverte"));
    }

    // ─────────────────────────────────────────────────────────────
    // getOperationsDuJour()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getOperationsDuJour_shouldReturnDtoWithEmptyOperationsWhenNoTransferts() {
        caisse.setTransferts(new ArrayList<>());

        when(userRepository.findByEmail("agent@okane.ma")).thenReturn(Optional.of(agent));
        when(caisseRepository.findByAgentAndDateCaisseAndStatut(agent, LocalDate.now(), StatutCaisse.OUVERTE))
                .thenReturn(Optional.of(caisse));

        CaisseResponseDTO result = caisseService.getOperationsDuJour("agent@okane.ma");

        assertNotNull(result);
        assertNotNull(result.getOperations());
        assertTrue(result.getOperations().isEmpty());
        assertEquals(0, result.getNombreOperations());
    }

    @Test
    void getOperationsDuJour_shouldMapTransfertToEncaissementWhenAgenceEnvoiMatchesAgent() {
        // Arrange : transfert dont l'agenceEnvoi est la même que l'agence de l'agent
        Client expediteur = new Client();
        expediteur.setNom("Martin");
        expediteur.setPrenom("Alice");

        Devise devise = new Devise();
        devise.setCode("MAD");

        Corridor corridor = new Corridor();
        corridor.setDeviseSource(devise);

        Transfert t = new Transfert();
        t.setId(200L);
        t.setCodeRetrait("ABC123");
        t.setMontantEnvoye(new BigDecimal("1000.00"));
        t.setDateCreation(LocalDateTime.now());
        t.setAgenceEnvoi(agence);       // même agence → ENCAISSEMENT
        t.setExpediteur(expediteur);
        t.setCorridor(corridor);

        caisse.setTransferts(List.of(t));

        when(userRepository.findByEmail("agent@okane.ma")).thenReturn(Optional.of(agent));
        when(caisseRepository.findByAgentAndDateCaisseAndStatut(agent, LocalDate.now(), StatutCaisse.OUVERTE))
                .thenReturn(Optional.of(caisse));

        CaisseResponseDTO result = caisseService.getOperationsDuJour("agent@okane.ma");

        assertEquals(1, result.getOperations().size());
        CaisseResponseDTO.OperationCaisseDTO op = result.getOperations().get(0);
        assertEquals(200L, op.getTransfertId());
        assertEquals("ENCAISSEMENT", op.getType());
        assertEquals("MAD", op.getDeviseCode());
        assertEquals("Martin Alice", op.getExpediteurOuBeneficiaire());
    }

    @Test
    void getOperationsDuJour_shouldMapTransfertToDecaissementWhenAgenceEnvoiDiffers() {
        Agence autreAgence = new Agence();
        autreAgence.setId(99L);
        autreAgence.setNom("Autre Agence");

        Client beneficiaire = new Client();
        beneficiaire.setNom("Lemaire");
        beneficiaire.setPrenom("Bob");

        Transfert t = new Transfert();
        t.setId(201L);
        t.setMontantEnvoye(new BigDecimal("500.00"));
        t.setDateCreation(LocalDateTime.now());
        t.setAgenceEnvoi(autreAgence);  // agence différente → DECAISSEMENT
        t.setBeneficiaire(beneficiaire);

        caisse.setTransferts(List.of(t));

        when(userRepository.findByEmail("agent@okane.ma")).thenReturn(Optional.of(agent));
        when(caisseRepository.findByAgentAndDateCaisseAndStatut(agent, LocalDate.now(), StatutCaisse.OUVERTE))
                .thenReturn(Optional.of(caisse));

        CaisseResponseDTO result = caisseService.getOperationsDuJour("agent@okane.ma");

        CaisseResponseDTO.OperationCaisseDTO op = result.getOperations().get(0);
        assertEquals("DECAISSEMENT", op.getType());
        assertEquals("Lemaire Bob",  op.getExpediteurOuBeneficiaire());
    }

    // ─────────────────────────────────────────────────────────────
    // cloturerCaisse()
    // ─────────────────────────────────────────────────────────────

    @Test
    void cloturerCaisse_shouldSetCaisseToFermeeAndCalculateEcart() {
        ClotureCaisseRequestDTO request = new ClotureCaisseRequestDTO();
        // soldeTheorique = ouverture(5000) + encaissements(2000) - decaissements(500) = 6500
        request.setSoldeCompte(new BigDecimal("6500.00"));

        when(userRepository.findByEmail("agent@okane.ma")).thenReturn(Optional.of(agent));
        when(caisseRepository.findByAgentAndDateCaisseAndStatut(agent, LocalDate.now(), StatutCaisse.OUVERTE))
                .thenReturn(Optional.of(caisse));
        when(caisseRepository.save(any(Caisse.class))).thenAnswer(inv -> inv.getArgument(0));

        CaisseResponseDTO result = caisseService.cloturerCaisse("agent@okane.ma", request);

        assertEquals(StatutCaisse.FERMEE, result.getStatut());
        assertEquals(new BigDecimal("6500.00"), result.getSoldeCloture());
        assertEquals(new BigDecimal("6500.00"), result.getSoldeTheorique());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getEcart()), "L'écart doit être zéro");    }

    @Test
    void cloturerCaisse_shouldDetectEcartWhenSoldesDiffer() {
        ClotureCaisseRequestDTO request = new ClotureCaisseRequestDTO();
        request.setSoldeCompte(new BigDecimal("6450.00")); // écart = -50

        when(userRepository.findByEmail("agent@okane.ma")).thenReturn(Optional.of(agent));
        when(caisseRepository.findByAgentAndDateCaisseAndStatut(agent, LocalDate.now(), StatutCaisse.OUVERTE))
                .thenReturn(Optional.of(caisse));
        when(caisseRepository.save(any(Caisse.class))).thenAnswer(inv -> inv.getArgument(0));

        caisseService.cloturerCaisse("agent@okane.ma", request);

        ArgumentCaptor<Caisse> captor = ArgumentCaptor.forClass(Caisse.class);
        verify(caisseRepository).save(captor.capture());
        Caisse saved = captor.getValue();




        assertTrue(saved.getEcartDetecte());
        assertEquals(0, new BigDecimal("-50.00").compareTo(saved.getEcart()));
    }

    @Test
    void cloturerCaisse_shouldSaveObservationWhenProvided() {
        ClotureCaisseRequestDTO request = new ClotureCaisseRequestDTO();
        request.setSoldeCompte(new BigDecimal("6500.00"));
        request.setObservation("RAS");

        when(userRepository.findByEmail("agent@okane.ma")).thenReturn(Optional.of(agent));
        when(caisseRepository.findByAgentAndDateCaisseAndStatut(agent, LocalDate.now(), StatutCaisse.OUVERTE))
                .thenReturn(Optional.of(caisse));
        when(caisseRepository.save(any(Caisse.class))).thenAnswer(inv -> inv.getArgument(0));

        caisseService.cloturerCaisse("agent@okane.ma", request);

        ArgumentCaptor<Caisse> captor = ArgumentCaptor.forClass(Caisse.class);
        verify(caisseRepository).save(captor.capture());
        assertEquals("RAS", captor.getValue().getObservation());
    }

    @Test
    void cloturerCaisse_shouldSetDateClotureToNow() {
        ClotureCaisseRequestDTO request = new ClotureCaisseRequestDTO();
        request.setSoldeCompte(new BigDecimal("6500.00"));

        when(userRepository.findByEmail("agent@okane.ma")).thenReturn(Optional.of(agent));
        when(caisseRepository.findByAgentAndDateCaisseAndStatut(agent, LocalDate.now(), StatutCaisse.OUVERTE))
                .thenReturn(Optional.of(caisse));
        when(caisseRepository.save(any(Caisse.class))).thenAnswer(inv -> inv.getArgument(0));

        caisseService.cloturerCaisse("agent@okane.ma", request);

        ArgumentCaptor<Caisse> captor = ArgumentCaptor.forClass(Caisse.class);
        verify(caisseRepository).save(captor.capture());
        assertNotNull(captor.getValue().getDateCloture());
    }

    // ─────────────────────────────────────────────────────────────
    // signalerEcart()
    // ─────────────────────────────────────────────────────────────

    @Test
    void signalerEcart_shouldPersistEcartAndSetFlagTrue() {
        EcartCaisseRequestDTO request = new EcartCaisseRequestDTO();
        request.setMontantEcart(new BigDecimal("200.00"));
        request.setMotif("Erreur de rendu monnaie");

        when(userRepository.findByEmail("agent@okane.ma")).thenReturn(Optional.of(agent));
        when(caisseRepository.findByAgentAndDateCaisseAndStatut(agent, LocalDate.now(), StatutCaisse.OUVERTE))
                .thenReturn(Optional.of(caisse));
        when(caisseRepository.save(any(Caisse.class))).thenAnswer(inv -> inv.getArgument(0));

        caisseService.signalerEcart("agent@okane.ma", request);

        ArgumentCaptor<Caisse> captor = ArgumentCaptor.forClass(Caisse.class);
        verify(caisseRepository).save(captor.capture());
        Caisse saved = captor.getValue();

        assertEquals(new BigDecimal("200.00"), saved.getEcart());
        assertEquals("Erreur de rendu monnaie", saved.getMotifEcart());
        assertTrue(saved.getEcartDetecte());
    }

    @Test
    void signalerEcart_shouldThrowWhenAgentNotFound() {
        when(userRepository.findByEmail("x@okane.ma")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> caisseService.signalerEcart("x@okane.ma", new EcartCaisseRequestDTO()));

        verify(caisseRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // getCaissesAgence()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getCaissesAgence_shouldReturnListOfDtosForManagerAgence() {
        User manager = new User();
        manager.setEmail("manager@okane.ma");
        manager.setAgence(agence);

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(caisseRepository.findByAgenceIdAndDate(1L, LocalDate.now()))
                .thenReturn(List.of(caisse));

        List<CaisseResponseDTO> result = caisseService.getCaissesAgence("manager@okane.ma");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
        verify(caisseRepository).findByAgenceIdAndDate(1L, LocalDate.now());
    }

    @Test
    void getCaissesAgence_shouldThrowWhenManagerHasNoAgence() {
        User managerSansAgence = new User();
        managerSansAgence.setEmail("nul@okane.ma");
        managerSansAgence.setAgence(null);

        when(userRepository.findByEmail("nul@okane.ma")).thenReturn(Optional.of(managerSansAgence));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> caisseService.getCaissesAgence("nul@okane.ma"));

        assertTrue(ex.getMessage().contains("aucune agence"));
        verify(caisseRepository, never()).findByAgenceIdAndDate(any(), any());
    }

    @Test
    void getCaissesAgence_shouldReturnEmptyListWhenNoCaisses() {
        User manager = new User();
        manager.setEmail("manager@okane.ma");
        manager.setAgence(agence);

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(caisseRepository.findByAgenceIdAndDate(1L, LocalDate.now()))
                .thenReturn(List.of());

        List<CaisseResponseDTO> result = caisseService.getCaissesAgence("manager@okane.ma");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}