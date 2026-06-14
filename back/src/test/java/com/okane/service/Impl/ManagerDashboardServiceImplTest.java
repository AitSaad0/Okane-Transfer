package com.okane.service.Impl;

import com.okane.dto.responseDto.ManagerDashboardResponseDTO;
import com.okane.entity.*;
import com.okane.entity.enums.StatutCaisse;
import com.okane.entity.enums.StatutTransfert;
import com.okane.exception.BadRequestException;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.CaisseRepository;
import com.okane.repository.TransfertRepository;
import com.okane.repository.UserRepository;
import com.okane.service.impl.ManagerDashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerDashboardServiceImplTest {

    @Mock private UserRepository      userRepository;
    @Mock private TransfertRepository transfertRepository;
    @Mock private CaisseRepository    caisseRepository;

    @InjectMocks
    private ManagerDashboardServiceImpl dashboardService;

    // ── fixtures ──────────────────────────────────────────────────

    private Pays   pays;
    private Agence agence;
    private User   manager;
    private User   agent1;

    @BeforeEach
    void setUp() {
        pays = new Pays();
        pays.setNom("Maroc");

        agence = new Agence();
        agence.setId(1L);
        agence.setNom("Agence Casa");
        agence.setVille("Casablanca");
        agence.setPays(pays);

        agent1 = new User();
        agent1.setId(20L);
        agent1.setNom("Dupont");
        agent1.setPrenom("Jean");
        agent1.setAgence(agence);

        manager = new User();
        manager.setId(10L);
        manager.setEmail("manager@okane.ma");
        manager.setAgence(agence);
    }

    // ── helpers ────────────────────────────────────────────────────

    /**
     * Construit un Transfert payé minimal pour les tests de statistiques.
     */
    private Transfert makeTransfertPaye(BigDecimal montant, BigDecimal frais, User agent) {
        Transfert t = new Transfert();
        t.setStatut(StatutTransfert.PAYE);
        t.setMontantEnvoye(montant);
        t.setFrais(frais);
        t.setAgentEnvoi(agent);
        t.setDateCreation(LocalDateTime.now());
        return t;
    }

    private Transfert makeTransfertEnAttente() {
        Transfert t = new Transfert();
        t.setStatut(StatutTransfert.EN_ATTENTE);
        t.setMontantEnvoye(new BigDecimal("300.00"));
        t.setFrais(BigDecimal.ZERO);
        t.setDateCreation(LocalDateTime.now());
        return t;
    }

    private void stubManagerAndEmptyPages() {
        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transfertRepository.findAllWithFilters(isNull(), eq(1L), isNull(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(caisseRepository.findByAgentOrderByDateOuvertureDesc(manager))
                .thenReturn(List.of());
    }

    // ─────────────────────────────────────────────────────────────
    // getDashboard()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getDashboard_shouldThrowWhenManagerNotFound() {
        when(userRepository.findByEmail("inconnu@okane.ma")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> dashboardService.getDashboard("inconnu@okane.ma"));
    }

    @Test
    void getDashboard_shouldThrowWhenManagerHasNoAgence() {
        User managerSansAgence = new User();
        managerSansAgence.setEmail("m@okane.ma");
        managerSansAgence.setAgence(null);

        when(userRepository.findByEmail("m@okane.ma")).thenReturn(Optional.of(managerSansAgence));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> dashboardService.getDashboard("m@okane.ma"));

        assertTrue(ex.getMessage().contains("aucune agence"));
    }

    @Test
    void getDashboard_shouldReturnAgenceIdentificationFields() {
        stubManagerAndEmptyPages();

        ManagerDashboardResponseDTO result = dashboardService.getDashboard("manager@okane.ma");

        assertEquals("Agence Casa", result.getAgenceNom());
        assertEquals("Casablanca",  result.getAgenceVille());
        assertEquals("Maroc",       result.getAgencePays());
    }

    @Test
    void getDashboard_shouldReturnZeroStatsWhenNoTransferts() {
        stubManagerAndEmptyPages();

        ManagerDashboardResponseDTO result = dashboardService.getDashboard("manager@okane.ma");

        assertEquals(BigDecimal.ZERO, result.getVolumeJour());
        assertEquals(BigDecimal.ZERO, result.getCommissionsJour());
        assertEquals(0.0,             result.getTauxSucces());
        assertEquals(0,               result.getNombreTransfertsJour());
        assertEquals(0,               result.getNombreTransfertsEnAttente());
        assertNotNull(result.getTopAgents());
        assertTrue(result.getTopAgents().isEmpty());
    }

    // ─────────────────────────────────────────────────────────────
    // getRapportJournalier()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getRapportJournalier_shouldUseProvidedDate() {
        String targetDate = "2025-03-15";

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transfertRepository.findAllWithFilters(isNull(), eq(1L), isNull(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(caisseRepository.findByAgentOrderByDateOuvertureDesc(manager))
                .thenReturn(List.of());

        ManagerDashboardResponseDTO result =
                dashboardService.getRapportJournalier("manager@okane.ma", targetDate);

        assertNotNull(result);
        // Les filtres doivent inclure les bornes de la date fournie
        LocalDateTime expectedDebut = LocalDate.parse(targetDate).atStartOfDay();
        verify(transfertRepository).findAllWithFilters(
                isNull(), eq(1L), isNull(),
                eq(expectedDebut),
                any(LocalDateTime.class),
                any(Pageable.class));
    }

    @Test
    void getRapportJournalier_shouldDefaultToTodayWhenDateIsNull() {
        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transfertRepository.findAllWithFilters(isNull(), eq(1L), isNull(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(caisseRepository.findByAgentOrderByDateOuvertureDesc(manager))
                .thenReturn(List.of());

        ManagerDashboardResponseDTO result =
                dashboardService.getRapportJournalier("manager@okane.ma", null);

        assertNotNull(result);
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        verify(transfertRepository).findAllWithFilters(
                isNull(), eq(1L), isNull(),
                eq(todayStart),
                any(LocalDateTime.class),
                any(Pageable.class));
    }

    // ─────────────────────────────────────────────────────────────
    // Calcul des volumes et commissions
    // ─────────────────────────────────────────────────────────────

    @Test
    void getDashboard_shouldSumVolumeOnlyFromPayedTransferts() {
        Transfert paye    = makeTransfertPaye(new BigDecimal("1000.00"), new BigDecimal("20.00"), agent1);
        Transfert attente = makeTransfertEnAttente();

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transfertRepository.findAllWithFilters(any(), eq(1L), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(paye, attente)));
        when(caisseRepository.findByAgentOrderByDateOuvertureDesc(manager))
                .thenReturn(List.of());

        ManagerDashboardResponseDTO result = dashboardService.getDashboard("manager@okane.ma");

        // Volume = uniquement le transfert PAYE
        assertEquals(new BigDecimal("1000.00"), result.getVolumeJour());
        assertEquals(new BigDecimal("20.00"),   result.getCommissionsJour());
    }

    @Test
    void getDashboard_shouldCountTotalAndPendingTransferts() {
        Transfert paye    = makeTransfertPaye(new BigDecimal("500.00"), BigDecimal.ZERO, agent1);
        Transfert attente = makeTransfertEnAttente();

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transfertRepository.findAllWithFilters(any(), eq(1L), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(paye, attente)));
        when(caisseRepository.findByAgentOrderByDateOuvertureDesc(manager))
                .thenReturn(List.of());

        ManagerDashboardResponseDTO result = dashboardService.getDashboard("manager@okane.ma");

        assertEquals(2, result.getNombreTransfertsJour());
        assertEquals(1, result.getNombreTransfertsEnAttente());
    }

    // ─────────────────────────────────────────────────────────────
    // Taux de succès
    // ─────────────────────────────────────────────────────────────

    @Test
    void getDashboard_shouldCalculateTauxSuccesCorrectly() {
        // 1 payé + 1 en attente sur 2 total → 50 %
        Transfert paye    = makeTransfertPaye(new BigDecimal("500.00"), BigDecimal.ZERO, agent1);
        Transfert attente = makeTransfertEnAttente();

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transfertRepository.findAllWithFilters(any(), eq(1L), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(paye, attente)));
        when(caisseRepository.findByAgentOrderByDateOuvertureDesc(manager))
                .thenReturn(List.of());

        ManagerDashboardResponseDTO result = dashboardService.getDashboard("manager@okane.ma");

        assertEquals(50.0, result.getTauxSucces());
    }

    @Test
    void getDashboard_shouldReturn100TauxSuccesWhenAllPaid() {
        Transfert p1 = makeTransfertPaye(new BigDecimal("200.00"), BigDecimal.ZERO, agent1);
        Transfert p2 = makeTransfertPaye(new BigDecimal("300.00"), BigDecimal.ZERO, agent1);

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transfertRepository.findAllWithFilters(any(), eq(1L), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(p1, p2)));
        when(caisseRepository.findByAgentOrderByDateOuvertureDesc(manager))
                .thenReturn(List.of());

        ManagerDashboardResponseDTO result = dashboardService.getDashboard("manager@okane.ma");

        assertEquals(100.0, result.getTauxSucces());
    }

    @Test
    void getDashboard_shouldReturn0TauxSuccesWhenNoTransferts() {
        stubManagerAndEmptyPages();

        ManagerDashboardResponseDTO result = dashboardService.getDashboard("manager@okane.ma");

        assertEquals(0.0, result.getTauxSucces());
    }

    // ─────────────────────────────────────────────────────────────
    // Agents actifs & solde caisses
    // ─────────────────────────────────────────────────────────────

    @Test
    void getDashboard_shouldCountOnlyOpenCaisses() {
        Caisse ouverte = new Caisse();
        ouverte.setStatut(StatutCaisse.OUVERTE);
        ouverte.setSoldeCourant(new BigDecimal("5000.00"));

        Caisse fermee = new Caisse();
        fermee.setStatut(StatutCaisse.FERMEE);
        fermee.setSoldeCourant(new BigDecimal("3000.00"));

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transfertRepository.findAllWithFilters(any(), eq(1L), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(caisseRepository.findByAgentOrderByDateOuvertureDesc(manager))
                .thenReturn(List.of(ouverte, fermee));

        ManagerDashboardResponseDTO result = dashboardService.getDashboard("manager@okane.ma");

        assertEquals(1, result.getNombreAgentsActifs());
        // Solde total = somme des deux caisses (ouvertes ET fermées)
        assertEquals(0, new BigDecimal("8000.00").compareTo(result.getSoldeCaisseTotal()));
    }

    @Test
    void getDashboard_shouldReturn0AgentsActifsWhenNoCaisses() {
        stubManagerAndEmptyPages();

        ManagerDashboardResponseDTO result = dashboardService.getDashboard("manager@okane.ma");

        assertEquals(0, result.getNombreAgentsActifs());
        assertEquals(BigDecimal.ZERO, result.getSoldeCaisseTotal());
    }

    // ─────────────────────────────────────────────────────────────
    // Top agents
    // ─────────────────────────────────────────────────────────────

    @Test
    void getDashboard_shouldBuildTopAgentsSortedByVolumeDesc() {
        User agent2 = new User();
        agent2.setId(21L);
        agent2.setNom("Bernard");
        agent2.setPrenom("Claire");

        Transfert t1 = makeTransfertPaye(new BigDecimal("1000.00"), new BigDecimal("10.00"), agent1);
        Transfert t2 = makeTransfertPaye(new BigDecimal("3000.00"), new BigDecimal("30.00"), agent2);
        Transfert t3 = makeTransfertPaye(new BigDecimal("500.00"),  new BigDecimal("5.00"),  agent1);

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transfertRepository.findAllWithFilters(any(), eq(1L), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(t1, t2, t3)));
        when(caisseRepository.findByAgentOrderByDateOuvertureDesc(manager))
                .thenReturn(List.of());

        ManagerDashboardResponseDTO result = dashboardService.getDashboard("manager@okane.ma");

        List<ManagerDashboardResponseDTO.AgentStatDTO> topAgents = result.getTopAgents();
        assertNotNull(topAgents);
        assertEquals(2, topAgents.size());

        // Agent2 (3000) doit être premier
        assertEquals(21L, topAgents.get(0).getAgentId());
        assertEquals(0, new BigDecimal("3000.00").compareTo(topAgents.get(0).getVolumeTraite()));
        assertEquals(1, topAgents.get(0).getNombreTransferts());

        // Agent1 (1000 + 500 = 1500) en second
        assertEquals(20L, topAgents.get(1).getAgentId());
        assertEquals(0, new BigDecimal("1500.00").compareTo(topAgents.get(1).getVolumeTraite()));
        assertEquals(2, topAgents.get(1).getNombreTransferts());
    }

    @Test
    void getDashboard_shouldIgnoreTransfertsWithoutAgent() {
        Transfert sansAgent = new Transfert();
        sansAgent.setStatut(StatutTransfert.PAYE);
        sansAgent.setMontantEnvoye(new BigDecimal("999.00"));
        sansAgent.setFrais(BigDecimal.ZERO);
        sansAgent.setAgentEnvoi(null);

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transfertRepository.findAllWithFilters(any(), eq(1L), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(sansAgent)));
        when(caisseRepository.findByAgentOrderByDateOuvertureDesc(manager))
                .thenReturn(List.of());

        ManagerDashboardResponseDTO result = dashboardService.getDashboard("manager@okane.ma");

        assertTrue(result.getTopAgents().isEmpty());
    }

    @Test
    void getDashboard_shouldLimitTopAgentsToFive() {
        List<Transfert> transferts = new java.util.ArrayList<>();
        for (int i = 0; i < 7; i++) {
            User agentN = new User();
            agentN.setId((long) (100 + i));
            agentN.setNom("Agent" + i);
            agentN.setPrenom("P" + i);
            BigDecimal montant = new BigDecimal((7 - i) * 1000 + ".00");
            transferts.add(makeTransfertPaye(montant, BigDecimal.ZERO, agentN));
        }

        when(userRepository.findByEmail("manager@okane.ma")).thenReturn(Optional.of(manager));
        when(transfertRepository.findAllWithFilters(any(), eq(1L), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(transferts));
        when(caisseRepository.findByAgentOrderByDateOuvertureDesc(manager))
                .thenReturn(List.of());

        ManagerDashboardResponseDTO result = dashboardService.getDashboard("manager@okane.ma");

        assertEquals(5, result.getTopAgents().size());
    }

    // ─────────────────────────────────────────────────────────────
    // exportRapport()
    // ─────────────────────────────────────────────────────────────

    @Test
    void exportRapport_shouldReturnCsvBytesForCsvFormat() {
        stubManagerAndEmptyPages();

        byte[] bytes = dashboardService.exportRapport("manager@okane.ma", "csv", null);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        String csv = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csv.contains("Agence"));
        assertTrue(csv.contains("Volume du jour"));
    }

    @Test
    void exportRapport_shouldReturnPdfBytesForPdfFormat() {
        stubManagerAndEmptyPages();

        byte[] bytes = dashboardService.exportRapport("manager@okane.ma", "pdf", null);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(content.contains("PDF Report"));
    }

    @Test
    void exportRapport_shouldThrowWhenFormatUnsupported() {
        stubManagerAndEmptyPages();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> dashboardService.exportRapport("manager@okane.ma", "xlsx", null));

        assertTrue(ex.getMessage().contains("Format non supporté"));
    }
}