package com.okane.repository;

import com.okane.entity.*;
import com.okane.entity.enums.Role;
import com.okane.entity.enums.StatutAgence;
import com.okane.entity.enums.StatutCaisse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CaisseRepositoryTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    @BeforeAll
    static void initFactory() {
        emf = Persistence.createEntityManagerFactory("test-pu");
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
    }

    @AfterEach
    void tearDown() {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Caisse").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.createQuery("DELETE FROM Agence").executeUpdate();
        em.createQuery("DELETE FROM Pays").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @AfterAll
    static void closeFactory() {
        emf.close();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Pays buildAndPersistPays(String codeIso, String nom) {
        Pays pays = Pays.builder().codeIso(codeIso).nom(nom).build();
        em.persist(pays);
        return pays;
    }

    private Agence buildAndPersistAgence(String nom, Pays pays) {
        Agence agence = Agence.builder()
                .nom(nom)
                .adresse("Rue Test")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("50000.00"))
                .statut(StatutAgence.ACTIVE)
                .pays(pays)
                .build();
        em.persist(agence);
        return agence;
    }

    private User buildAndPersistAgent(String email, Agence agence) {
        User agent = User.builder()
                .email(email)
                .password("encodedPassword")
                .nom("Agent")
                .prenom("Test")
                .role(Role.AGENT)
                .active(true)
                .deleted(false)
                .agence(agence)
                .build();
        em.persist(agent);
        return agent;
    }

    private Caisse buildAndPersistCaisse(User agent, Agence agence,
                                         LocalDate dateCaisse, StatutCaisse statut) {
        Caisse caisse = Caisse.builder()
                .agent(agent)
                .agence(agence)
                .dateCaisse(dateCaisse)
                .dateOuverture(dateCaisse.atTime(8, 0))
                .soldeCourant(new BigDecimal("5000.00"))
                .soldeOuverture(new BigDecimal("5000.00"))
                .totalEncaissements(new BigDecimal("2000.00"))
                .totalDecaissements(new BigDecimal("500.00"))
                .statut(statut)
                .build();
        em.persist(caisse);
        return caisse;
    }

    // ── findByAgentAndDateCaisseAndStatut() ───────────────────────

    @Test
    void findByAgentAndDateCaisseAndStatut_shouldReturnCaisseWhenAllMatch() {
        em.getTransaction().begin();
        Pays pays     = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Casa", pays);
        User agent    = buildAndPersistAgent("agent@okane.com", agence);
        buildAndPersistCaisse(agent, agence, LocalDate.now(), StatutCaisse.OUVERTE);
        em.getTransaction().commit();
        Long agentId = agent.getId();
        em.clear();

        Long count = em.createQuery("""
                SELECT COUNT(c) FROM Caisse c
                WHERE c.agent.id = :agentId
                  AND c.dateCaisse = :date
                  AND c.statut = :statut
                """, Long.class)
                .setParameter("agentId", agentId)
                .setParameter("date", LocalDate.now())
                .setParameter("statut", StatutCaisse.OUVERTE)
                .getSingleResult();

        assertTrue(count > 0);
    }

    @Test
    void findByAgentAndDateCaisseAndStatut_shouldReturnEmptyWhenDateDoesNotMatch() {
        em.getTransaction().begin();
        Pays pays     = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Casa", pays);
        User agent    = buildAndPersistAgent("agent@okane.com", agence);
        buildAndPersistCaisse(agent, agence, LocalDate.now().minusDays(1), StatutCaisse.OUVERTE);
        em.getTransaction().commit();
        Long agentId = agent.getId();
        em.clear();

        Long count = em.createQuery("""
                SELECT COUNT(c) FROM Caisse c
                WHERE c.agent.id = :agentId
                  AND c.dateCaisse = :date
                  AND c.statut = :statut
                """, Long.class)
                .setParameter("agentId", agentId)
                .setParameter("date", LocalDate.now())
                .setParameter("statut", StatutCaisse.OUVERTE)
                .getSingleResult();

        assertFalse(count > 0);
    }

    @Test
    void findByAgentAndDateCaisseAndStatut_shouldReturnEmptyWhenStatutDoesNotMatch() {
        em.getTransaction().begin();
        Pays pays     = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Casa", pays);
        User agent    = buildAndPersistAgent("agent@okane.com", agence);
        buildAndPersistCaisse(agent, agence, LocalDate.now(), StatutCaisse.FERMEE);
        em.getTransaction().commit();
        Long agentId = agent.getId();
        em.clear();

        Long count = em.createQuery("""
                SELECT COUNT(c) FROM Caisse c
                WHERE c.agent.id = :agentId
                  AND c.dateCaisse = :date
                  AND c.statut = :statut
                """, Long.class)
                .setParameter("agentId", agentId)
                .setParameter("date", LocalDate.now())
                .setParameter("statut", StatutCaisse.OUVERTE)
                .getSingleResult();

        assertFalse(count > 0);
    }

    @Test
    void findByAgentAndDateCaisseAndStatut_shouldReturnEmptyWhenAgentDoesNotMatch() {
        em.getTransaction().begin();
        Pays pays     = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Casa", pays);
        User agent1   = buildAndPersistAgent("agent1@okane.com", agence);
        User agent2   = buildAndPersistAgent("agent2@okane.com", agence);
        buildAndPersistCaisse(agent1, agence, LocalDate.now(), StatutCaisse.OUVERTE);
        em.getTransaction().commit();
        Long agent2Id = agent2.getId();
        em.clear();

        Long count = em.createQuery("""
                SELECT COUNT(c) FROM Caisse c
                WHERE c.agent.id = :agentId
                  AND c.dateCaisse = :date
                  AND c.statut = :statut
                """, Long.class)
                .setParameter("agentId", agent2Id)
                .setParameter("date", LocalDate.now())
                .setParameter("statut", StatutCaisse.OUVERTE)
                .getSingleResult();

        assertFalse(count > 0);
    }

    // ── findByAgenceIdAndDate() ───────────────────────────────────

    @Test
    void findByAgenceIdAndDate_shouldReturnAllCaissesOfAgenceForToday() {
        em.getTransaction().begin();
        Pays pays     = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Casa", pays);
        User agent1   = buildAndPersistAgent("agent1@okane.com", agence);
        User agent2   = buildAndPersistAgent("agent2@okane.com", agence);
        buildAndPersistCaisse(agent1, agence, LocalDate.now(), StatutCaisse.OUVERTE);
        buildAndPersistCaisse(agent2, agence, LocalDate.now(), StatutCaisse.OUVERTE);
        // Hier — ne doit pas être retournée
        buildAndPersistCaisse(agent1, agence, LocalDate.now().minusDays(1), StatutCaisse.FERMEE);
        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        Long count = em.createQuery("""
                SELECT COUNT(c) FROM Caisse c
                WHERE c.agence.id = :agenceId
                  AND c.dateCaisse = :date
                """, Long.class)
                .setParameter("agenceId", agenceId)
                .setParameter("date", LocalDate.now())
                .getSingleResult();

        assertEquals(2L, count);
    }

    @Test
    void findByAgenceIdAndDate_shouldReturnEmptyWhenNoCaissesToday() {
        em.getTransaction().begin();
        Pays pays     = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Casa", pays);
        User agent    = buildAndPersistAgent("agent@okane.com", agence);
        buildAndPersistCaisse(agent, agence, LocalDate.now().minusDays(1), StatutCaisse.FERMEE);
        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        Long count = em.createQuery("""
                SELECT COUNT(c) FROM Caisse c
                WHERE c.agence.id = :agenceId
                  AND c.dateCaisse = :date
                """, Long.class)
                .setParameter("agenceId", agenceId)
                .setParameter("date", LocalDate.now())
                .getSingleResult();

        assertEquals(0L, count);
    }

    @Test
    void findByAgenceIdAndDate_shouldNotReturnCaissesFromOtherAgence() {
        em.getTransaction().begin();
        Pays pays      = buildAndPersistPays("MAR", "Maroc");
        Agence agence1 = buildAndPersistAgence("Agence Casa",  pays);
        Agence agence2 = buildAndPersistAgence("Agence Rabat", pays);
        User agent1    = buildAndPersistAgent("agent1@okane.com", agence1);
        User agent2    = buildAndPersistAgent("agent2@okane.com", agence2);
        buildAndPersistCaisse(agent1, agence1, LocalDate.now(), StatutCaisse.OUVERTE);
        buildAndPersistCaisse(agent2, agence2, LocalDate.now(), StatutCaisse.OUVERTE);
        em.getTransaction().commit();
        Long agence1Id = agence1.getId();
        em.clear();

        Long count = em.createQuery("""
                SELECT COUNT(c) FROM Caisse c
                WHERE c.agence.id = :agenceId
                  AND c.dateCaisse = :date
                """, Long.class)
                .setParameter("agenceId", agence1Id)
                .setParameter("date", LocalDate.now())
                .getSingleResult();

        assertEquals(1L, count);
    }

    // ── findByAgentOrderByDateOuvertureDesc() ─────────────────────

    @Test
    void findByAgentOrderByDateOuvertureDesc_shouldReturnHistoryMostRecentFirst() {
        em.getTransaction().begin();
        Pays pays     = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Casa", pays);
        User agent    = buildAndPersistAgent("agent@okane.com", agence);
        buildAndPersistCaisse(agent, agence, LocalDate.now().minusDays(2), StatutCaisse.FERMEE);
        buildAndPersistCaisse(agent, agence, LocalDate.now().minusDays(1), StatutCaisse.FERMEE);
        buildAndPersistCaisse(agent, agence, LocalDate.now(),              StatutCaisse.OUVERTE);
        em.getTransaction().commit();
        Long agentId = agent.getId();
        em.clear();

        List<Caisse> result = em.createQuery("""
                SELECT c FROM Caisse c
                WHERE c.agent.id = :agentId
                ORDER BY c.dateOuverture DESC
                """, Caisse.class)
                .setParameter("agentId", agentId)
                .getResultList();

        assertEquals(3, result.size());
        assertEquals(LocalDate.now(),              result.get(0).getDateCaisse());
        assertEquals(LocalDate.now().minusDays(1), result.get(1).getDateCaisse());
        assertEquals(LocalDate.now().minusDays(2), result.get(2).getDateCaisse());
    }

    @Test
    void findByAgentOrderByDateOuvertureDesc_shouldReturnEmptyListWhenNoCaisses() {
        em.getTransaction().begin();
        Pays pays     = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Casa", pays);
        User agent    = buildAndPersistAgent("agent@okane.com", agence);
        em.getTransaction().commit();
        Long agentId = agent.getId();
        em.clear();

        List<Caisse> result = em.createQuery("""
                SELECT c FROM Caisse c
                WHERE c.agent.id = :agentId
                ORDER BY c.dateOuverture DESC
                """, Caisse.class)
                .setParameter("agentId", agentId)
                .getResultList();

        assertTrue(result.isEmpty());
    }

    // ── findByAgentAndStatut() ────────────────────────────────────

    @Test
    void findByAgentAndStatut_shouldFindOpenCaisseRegardlessOfDate() {
        em.getTransaction().begin();
        Pays pays     = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Casa", pays);
        User agent    = buildAndPersistAgent("agent@okane.com", agence);
        buildAndPersistCaisse(agent, agence, LocalDate.now(), StatutCaisse.OUVERTE);
        em.getTransaction().commit();
        Long agentId = agent.getId();
        em.clear();

        Long count = em.createQuery("""
                SELECT COUNT(c) FROM Caisse c
                WHERE c.agent.id = :agentId
                  AND c.statut = :statut
                """, Long.class)
                .setParameter("agentId", agentId)
                .setParameter("statut", StatutCaisse.OUVERTE)
                .getSingleResult();

        assertTrue(count > 0);
    }

    @Test
    void findByAgentAndStatut_shouldReturnEmptyWhenNoMatchingStatut() {
        em.getTransaction().begin();
        Pays pays     = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Casa", pays);
        User agent    = buildAndPersistAgent("agent@okane.com", agence);
        buildAndPersistCaisse(agent, agence, LocalDate.now(), StatutCaisse.FERMEE);
        em.getTransaction().commit();
        Long agentId = agent.getId();
        em.clear();

        Long count = em.createQuery("""
                SELECT COUNT(c) FROM Caisse c
                WHERE c.agent.id = :agentId
                  AND c.statut = :statut
                """, Long.class)
                .setParameter("agentId", agentId)
                .setParameter("statut", StatutCaisse.OUVERTE)
                .getSingleResult();

        assertFalse(count > 0);
    }

    // ── Cohérence des champs financiers ──────────────────────────

    @Test
    void caisse_shouldPersistAllFinancialFieldsCorrectly() {
        em.getTransaction().begin();
        Pays pays     = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Casa", pays);
        User agent    = buildAndPersistAgent("agent@okane.com", agence);

        Caisse caisse = Caisse.builder()
                .agent(agent)
                .agence(agence)
                .dateCaisse(LocalDate.now())
                .dateOuverture(LocalDateTime.now().withHour(8))
                .soldeCourant(new BigDecimal("6500.00"))
                .soldeOuverture(new BigDecimal("5000.00"))
                .soldeCloture(new BigDecimal("6500.00"))
                .soldeTheorique(new BigDecimal("6500.00"))
                .totalEncaissements(new BigDecimal("2000.00"))
                .totalDecaissements(new BigDecimal("500.00"))
                .ecart(BigDecimal.ZERO)
                .motifEcart(null)
                .ecartDetecte(false)
                .observation("RAS")
                .statut(StatutCaisse.FERMEE)
                .build();

        em.persist(caisse);
        em.getTransaction().commit();
        Long id = caisse.getId();
        em.clear();

        Caisse loaded = em.find(Caisse.class, id);

        assertNotNull(loaded);
        assertEquals(0, new BigDecimal("5000.00").compareTo(loaded.getSoldeOuverture()));
        assertEquals(0, new BigDecimal("6500.00").compareTo(loaded.getSoldeCloture()));
        assertEquals(0, new BigDecimal("6500.00").compareTo(loaded.getSoldeTheorique()));
        assertEquals(0, new BigDecimal("2000.00").compareTo(loaded.getTotalEncaissements()));
        assertEquals(0, new BigDecimal("500.00").compareTo(loaded.getTotalDecaissements()));
        assertEquals(0, BigDecimal.ZERO.compareTo(loaded.getEcart()));
        assertFalse(loaded.getEcartDetecte());
        assertEquals("RAS", loaded.getObservation());
        assertEquals(StatutCaisse.FERMEE, loaded.getStatut());
    }

    @Test
    void caisse_shouldPersistEcartDetecteWhenSet() {
        em.getTransaction().begin();
        Pays pays     = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Casa", pays);
        User agent    = buildAndPersistAgent("agent@okane.com", agence);

        Caisse caisse = Caisse.builder()
                .agent(agent)
                .agence(agence)
                .dateCaisse(LocalDate.now())
                .dateOuverture(LocalDateTime.now().withHour(8))
                .soldeCourant(new BigDecimal("4950.00"))
                .soldeOuverture(new BigDecimal("5000.00"))
                .ecart(new BigDecimal("-50.00"))
                .motifEcart("Erreur rendu monnaie")
                .ecartDetecte(true)
                .statut(StatutCaisse.FERMEE)
                .build();

        em.persist(caisse);
        em.getTransaction().commit();
        Long id = caisse.getId();
        em.clear();

        Caisse loaded = em.find(Caisse.class, id);

        assertTrue(loaded.getEcartDetecte());
        assertEquals(0, new BigDecimal("-50.00").compareTo(loaded.getEcart()));
        assertEquals("Erreur rendu monnaie", loaded.getMotifEcart());
    }
}