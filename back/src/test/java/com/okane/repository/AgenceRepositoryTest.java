package com.okane.repository;

import com.okane.entity.*;
import com.okane.entity.enums.Role;
import com.okane.entity.enums.StatutAgence;
import com.okane.entity.enums.StatutTransfert;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AgenceRepositoryTest {

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
        em.createQuery("DELETE FROM Transfert").executeUpdate();
        em.createQuery("DELETE FROM GrilleTarifaire").executeUpdate();
        em.createQuery("DELETE FROM Client").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.createQuery("DELETE FROM Corridor").executeUpdate();
        em.createQuery("DELETE FROM Agence").executeUpdate();
        em.createQuery("DELETE FROM Devise").executeUpdate();
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

    private Devise buildAndPersistDevise(String code) {
        Devise devise = Devise.builder()
                .code(code).symbole(code).nom("Devise " + code).build();
        em.persist(devise);
        return devise;
    }

    private Agence buildAndPersistAgence(String nom, String adresse, Pays pays, StatutAgence statut) {
        Agence agence = Agence.builder()
                .nom(nom).adresse(adresse)
                .plafondJournalier(new BigDecimal("50000.00"))
                .statut(statut).ville("Casablanca").codePostal("20000").pays(pays)
                .build();
        em.persist(agence);
        return agence;
    }

    private Corridor buildAndPersistCorridor(Pays origine, Pays destination,
                                             Devise deviseSource, Devise deviseDestination) {
        Corridor corridor = Corridor.builder()
                .tauxChange(new BigDecimal("10.500000")).actif(true)
                .paysOrigine(origine).paysDestination(destination)
                .deviseSource(deviseSource).deviseDestination(deviseDestination)
                .build();
        em.persist(corridor);
        return corridor;
    }

    private GrilleTarifaire buildAndPersistGrille(Corridor corridor,
                                                  BigDecimal min, BigDecimal max,
                                                  double partAgence) {
        GrilleTarifaire grille = GrilleTarifaire.builder()
                .corridor(corridor).montantMin(min).montantMax(max)
                .fraisFixe(new BigDecimal("5.00")).pourcentageFrais(2.0)
                .partAgence(partAgence)
                .build();
        em.persist(grille);
        return grille;
    }

    private User buildAndPersistAgent(String email, Agence agence) {
        User agent = User.builder()
                .email(email).password("encodedPassword")
                .nom("Agent").prenom("Test")
                .role(Role.AGENT).active(true).deleted(false).agence(agence)
                .build();
        em.persist(agent);
        return agent;
    }

    private Client buildAndPersistClient(String numPiece, String email, Pays pays) {
        Client client = Client.builder()
                .nom("Client").prenom("Test")
                .numPieceIdentite(numPiece).telephone("0600000000")
                .email(email).estSurListeSurveillance(false).deleted(false).pays(pays)
                .build();
        em.persist(client);
        return client;
    }

    private Transfert buildAndPersistTransfert(String codeRetrait,
                                               BigDecimal montantEnvoye, BigDecimal montantNet,
                                               StatutTransfert statut,
                                               LocalDateTime dateCreation, LocalDateTime datePaiement,
                                               Agence agenceEnvoi, Agence agencePaiement,
                                               Corridor corridor,
                                               Client expediteur, Client beneficiaire,
                                               User agentEnvoi) {
        Transfert t = Transfert.builder()
                .codeRetrait(codeRetrait).montantEnvoye(montantEnvoye)
                .frais(new BigDecimal("10.00")).montantNet(montantNet)
                .statut(statut).estSuspect(false)
                .dateCreation(dateCreation).datePaiement(datePaiement)
                .agenceEnvoi(agenceEnvoi).agencePaiement(agencePaiement)
                .corridor(corridor).expediteur(expediteur).beneficiaire(beneficiaire)
                .agentEnvoi(agentEnvoi)
                .build();
        em.persist(t);
        return t;
    }

    // ── existsByNomAndAdresse() ───────────────────────────────────

    @Test
    void existsByNomAndAdresse_shouldReturnTrueWhenExists() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays("MAR", "Maroc");
        buildAndPersistAgence("Agence Rabat", "12 Avenue Mohammed V", pays, StatutAgence.ACTIVE);
        em.getTransaction().commit();
        em.clear();

        Long count = em.createQuery(
                        "SELECT COUNT(a) FROM Agence a WHERE a.nom = :nom AND a.adresse = :adresse", Long.class)
                .setParameter("nom", "Agence Rabat")
                .setParameter("adresse", "12 Avenue Mohammed V")
                .getSingleResult();

        assertTrue(count > 0);
    }

    @Test
    void existsByNomAndAdresse_shouldReturnFalseWhenNotExists() {
        Long count = em.createQuery(
                        "SELECT COUNT(a) FROM Agence a WHERE a.nom = :nom AND a.adresse = :adresse", Long.class)
                .setParameter("nom", "Agence Inconnue")
                .setParameter("adresse", "Adresse Inconnue")
                .getSingleResult();

        assertFalse(count > 0);
    }

    @Test
    void existsByNomAndAdresse_shouldReturnFalseWhenOnlyNomMatches() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays("MAR", "Maroc");
        buildAndPersistAgence("Agence Rabat", "12 Avenue Mohammed V", pays, StatutAgence.ACTIVE);
        em.getTransaction().commit();
        em.clear();

        Long count = em.createQuery(
                        "SELECT COUNT(a) FROM Agence a WHERE a.nom = :nom AND a.adresse = :adresse", Long.class)
                .setParameter("nom", "Agence Rabat")
                .setParameter("adresse", "Autre Adresse")
                .getSingleResult();

        assertFalse(count > 0);
    }

    // ── findByPaysId / findByStatut / findByPaysIdAndStatut ───────

    @Test
    void findByPaysId_shouldReturnAgencesOfPays() {
        em.getTransaction().begin();
        Pays maroc  = buildAndPersistPays("MAR", "Maroc");
        Pays france = buildAndPersistPays("FRA", "France");
        buildAndPersistAgence("Agence Casa",  "Rue 1", maroc,  StatutAgence.ACTIVE);
        buildAndPersistAgence("Agence Rabat", "Rue 2", maroc,  StatutAgence.ACTIVE);
        buildAndPersistAgence("Agence Paris", "Rue 3", france, StatutAgence.ACTIVE);
        em.getTransaction().commit();
        Long marocId = maroc.getId();
        em.clear();

        Long count = em.createQuery(
                        "SELECT COUNT(a) FROM Agence a WHERE a.pays.id = :paysId", Long.class)
                .setParameter("paysId", marocId)
                .getSingleResult();

        assertEquals(2L, count);
    }

    @Test
    void findByStatut_shouldReturnOnlyAgencesWithGivenStatut() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays("MAR", "Maroc");
        buildAndPersistAgence("Agence Active 1",  "Rue 1", pays, StatutAgence.ACTIVE);
        buildAndPersistAgence("Agence Active 2",  "Rue 2", pays, StatutAgence.ACTIVE);
        buildAndPersistAgence("Agence Suspendue", "Rue 3", pays, StatutAgence.SUSPENDUE);
        em.getTransaction().commit();
        em.clear();

        Long countActive = em.createQuery(
                        "SELECT COUNT(a) FROM Agence a WHERE a.statut = :statut", Long.class)
                .setParameter("statut", StatutAgence.ACTIVE).getSingleResult();

        Long countSuspendue = em.createQuery(
                        "SELECT COUNT(a) FROM Agence a WHERE a.statut = :statut", Long.class)
                .setParameter("statut", StatutAgence.SUSPENDUE).getSingleResult();

        assertEquals(2L, countActive);
        assertEquals(1L, countSuspendue);
    }

    @Test
    void findByPaysIdAndStatut_shouldFilterByBothPaysAndStatut() {
        em.getTransaction().begin();
        Pays maroc  = buildAndPersistPays("MAR", "Maroc");
        Pays france = buildAndPersistPays("FRA", "France");
        buildAndPersistAgence("Agence Casa Active",     "Rue 1", maroc,  StatutAgence.ACTIVE);
        buildAndPersistAgence("Agence Rabat Suspendue", "Rue 2", maroc,  StatutAgence.SUSPENDUE);
        buildAndPersistAgence("Agence Paris Active",    "Rue 3", france, StatutAgence.ACTIVE);
        em.getTransaction().commit();
        Long marocId = maroc.getId();
        em.clear();

        Long count = em.createQuery(
                        "SELECT COUNT(a) FROM Agence a WHERE a.pays.id = :paysId AND a.statut = :statut", Long.class)
                .setParameter("paysId", marocId)
                .setParameter("statut", StatutAgence.ACTIVE)
                .getSingleResult();

        assertEquals(1L, count);
    }

    // ── sumVolumeEnvoiJour() ──────────────────────────────────────

    @Test
    void sumVolumeEnvoiJour_shouldSumNonAnnuleTransferts() {
        em.getTransaction().begin();
        Pays maroc  = buildAndPersistPays("MAR", "Maroc");
        Pays france = buildAndPersistPays("FRA", "France");
        Devise mad  = buildAndPersistDevise("MAD");
        Devise eur  = buildAndPersistDevise("EUR");
        Agence agence     = buildAndPersistAgence("Agence Test", "Rue 1", maroc, StatutAgence.ACTIVE);
        Corridor corridor = buildAndPersistCorridor(maroc, france, mad, eur);
        User agent        = buildAndPersistAgent("agent@okane.com", agence);
        Client expediteur  = buildAndPersistClient("ID001", "exp@okane.com", maroc);
        Client beneficiaire = buildAndPersistClient("ID002", "ben@okane.com", france);

        LocalDateTime debutJour = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        buildAndPersistTransfert("CODE001", new BigDecimal("1000.00"), new BigDecimal("990.00"),
                StatutTransfert.EN_ATTENTE, LocalDateTime.now(), null,
                agence, null, corridor, expediteur, beneficiaire, agent);

        buildAndPersistTransfert("CODE002", new BigDecimal("500.00"), new BigDecimal("490.00"),
                StatutTransfert.PAYE, LocalDateTime.now(), LocalDateTime.now(),
                agence, agence, corridor, expediteur, beneficiaire, agent);

        buildAndPersistTransfert("CODE003", new BigDecimal("200.00"), new BigDecimal("190.00"),
                StatutTransfert.ANNULE, LocalDateTime.now(), null,
                agence, null, corridor, expediteur, beneficiaire, agent);

        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        BigDecimal sum = em.createQuery("""
                SELECT COALESCE(SUM(t.montantEnvoye), 0)
                FROM Transfert t
                WHERE t.agenceEnvoi.id = :agenceId
                  AND t.dateCreation >= :debutJour
                  AND t.statut <> com.okane.entity.enums.StatutTransfert.ANNULE
                """, BigDecimal.class)
                .setParameter("agenceId", agenceId)
                .setParameter("debutJour", debutJour)
                .getSingleResult();

        assertEquals(0, new BigDecimal("1500.00").compareTo(sum));
    }

    @Test
    void sumVolumeEnvoiJour_shouldReturnZeroWhenNoTransferts() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Vide", "Rue 1", pays, StatutAgence.ACTIVE);
        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        LocalDateTime debutJour = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        BigDecimal sum = em.createQuery("""
                SELECT COALESCE(SUM(t.montantEnvoye), 0)
                FROM Transfert t
                WHERE t.agenceEnvoi.id = :agenceId
                  AND t.dateCreation >= :debutJour
                  AND t.statut <> com.okane.entity.enums.StatutTransfert.ANNULE
                """, BigDecimal.class)
                .setParameter("agenceId", agenceId)
                .setParameter("debutJour", debutJour)
                .getSingleResult();

        assertEquals(0, BigDecimal.ZERO.compareTo(sum));
    }

    @Test
    void sumVolumeEnvoiJour_shouldExcludeTransfertsFromPreviousDays() {
        em.getTransaction().begin();
        Pays maroc  = buildAndPersistPays("MAR", "Maroc");
        Pays france = buildAndPersistPays("FRA", "France");
        Devise mad  = buildAndPersistDevise("MAD");
        Devise eur  = buildAndPersistDevise("EUR");
        Agence agence     = buildAndPersistAgence("Agence Test", "Rue 1", maroc, StatutAgence.ACTIVE);
        Corridor corridor = buildAndPersistCorridor(maroc, france, mad, eur);
        User agent        = buildAndPersistAgent("agent@okane.com", agence);
        Client expediteur  = buildAndPersistClient("ID001", "exp@okane.com", maroc);
        Client beneficiaire = buildAndPersistClient("ID002", "ben@okane.com", france);

        LocalDateTime debutJour = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime hier      = debutJour.minusDays(1);

        buildAndPersistTransfert("CODE001", new BigDecimal("1000.00"), new BigDecimal("990.00"),
                StatutTransfert.EN_ATTENTE, hier, null,
                agence, null, corridor, expediteur, beneficiaire, agent);

        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        BigDecimal sum = em.createQuery("""
                SELECT COALESCE(SUM(t.montantEnvoye), 0)
                FROM Transfert t
                WHERE t.agenceEnvoi.id = :agenceId
                  AND t.dateCreation >= :debutJour
                  AND t.statut <> com.okane.entity.enums.StatutTransfert.ANNULE
                """, BigDecimal.class)
                .setParameter("agenceId", agenceId)
                .setParameter("debutJour", debutJour)
                .getSingleResult();

        assertEquals(0, BigDecimal.ZERO.compareTo(sum));
    }

    // ── sumVolumePaiementJour() ───────────────────────────────────

    @Test
    void sumVolumePaiementJour_shouldSumOnlyPayeTransferts() {
        em.getTransaction().begin();
        Pays maroc  = buildAndPersistPays("MAR", "Maroc");
        Pays france = buildAndPersistPays("FRA", "France");
        Devise mad  = buildAndPersistDevise("MAD");
        Devise eur  = buildAndPersistDevise("EUR");
        Agence agence     = buildAndPersistAgence("Agence Test", "Rue 1", maroc, StatutAgence.ACTIVE);
        Corridor corridor = buildAndPersistCorridor(maroc, france, mad, eur);
        User agent        = buildAndPersistAgent("agent@okane.com", agence);
        Client expediteur  = buildAndPersistClient("ID001", "exp@okane.com", maroc);
        Client beneficiaire = buildAndPersistClient("ID002", "ben@okane.com", france);

        LocalDateTime debutJour = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        buildAndPersistTransfert("CODE001", new BigDecimal("1000.00"), new BigDecimal("990.00"),
                StatutTransfert.PAYE, LocalDateTime.now(), LocalDateTime.now(),
                agence, agence, corridor, expediteur, beneficiaire, agent);

        buildAndPersistTransfert("CODE002", new BigDecimal("500.00"), new BigDecimal("490.00"),
                StatutTransfert.EN_ATTENTE, LocalDateTime.now(), null,
                agence, agence, corridor, expediteur, beneficiaire, agent);

        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        BigDecimal sum = em.createQuery("""
                SELECT COALESCE(SUM(t.montantNet), 0)
                FROM Transfert t
                WHERE t.agencePaiement.id = :agenceId
                  AND t.datePaiement >= :debutJour
                  AND t.statut = com.okane.entity.enums.StatutTransfert.PAYE
                """, BigDecimal.class)
                .setParameter("agenceId", agenceId)
                .setParameter("debutJour", debutJour)
                .getSingleResult();

        assertEquals(0, new BigDecimal("990.00").compareTo(sum));
    }

    @Test
    void sumVolumePaiementJour_shouldReturnZeroWhenNoPayeTransferts() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Vide", "Rue 1", pays, StatutAgence.ACTIVE);
        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        LocalDateTime debutJour = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        BigDecimal sum = em.createQuery("""
                SELECT COALESCE(SUM(t.montantNet), 0)
                FROM Transfert t
                WHERE t.agencePaiement.id = :agenceId
                  AND t.datePaiement >= :debutJour
                  AND t.statut = com.okane.entity.enums.StatutTransfert.PAYE
                """, BigDecimal.class)
                .setParameter("agenceId", agenceId)
                .setParameter("debutJour", debutJour)
                .getSingleResult();

        assertEquals(0, BigDecimal.ZERO.compareTo(sum));
    }

    // ── countTransfertsJour() ─────────────────────────────────────

    @Test
    void countTransfertsJour_shouldCountAllTransfertsOfDay() {
        em.getTransaction().begin();
        Pays maroc  = buildAndPersistPays("MAR", "Maroc");
        Pays france = buildAndPersistPays("FRA", "France");
        Devise mad  = buildAndPersistDevise("MAD");
        Devise eur  = buildAndPersistDevise("EUR");
        Agence agence     = buildAndPersistAgence("Agence Test", "Rue 1", maroc, StatutAgence.ACTIVE);
        Corridor corridor = buildAndPersistCorridor(maroc, france, mad, eur);
        User agent        = buildAndPersistAgent("agent@okane.com", agence);
        Client expediteur  = buildAndPersistClient("ID001", "exp@okane.com", maroc);
        Client beneficiaire = buildAndPersistClient("ID002", "ben@okane.com", france);

        LocalDateTime debutJour = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime hier      = debutJour.minusDays(1);

        buildAndPersistTransfert("CODE001", new BigDecimal("500.00"), new BigDecimal("490.00"),
                StatutTransfert.EN_ATTENTE, LocalDateTime.now(), null,
                agence, null, corridor, expediteur, beneficiaire, agent);

        buildAndPersistTransfert("CODE002", new BigDecimal("300.00"), new BigDecimal("290.00"),
                StatutTransfert.PAYE, LocalDateTime.now(), LocalDateTime.now(),
                agence, agence, corridor, expediteur, beneficiaire, agent);

        buildAndPersistTransfert("CODE003", new BigDecimal("200.00"), new BigDecimal("190.00"),
                StatutTransfert.EN_ATTENTE, hier, null,
                agence, null, corridor, expediteur, beneficiaire, agent);

        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        Long count = em.createQuery("""
                SELECT COUNT(t)
                FROM Transfert t
                WHERE t.agenceEnvoi.id = :agenceId
                  AND t.dateCreation >= :debutJour
                """, Long.class)
                .setParameter("agenceId", agenceId)
                .setParameter("debutJour", debutJour)
                .getSingleResult();

        assertEquals(2L, count);
    }

    @Test
    void countTransfertsJour_shouldReturnZeroWhenNoTransfertsToday() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Vide", "Rue 1", pays, StatutAgence.ACTIVE);
        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        LocalDateTime debutJour = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        Long count = em.createQuery("""
                SELECT COUNT(t)
                FROM Transfert t
                WHERE t.agenceEnvoi.id = :agenceId
                  AND t.dateCreation >= :debutJour
                """, Long.class)
                .setParameter("agenceId", agenceId)
                .setParameter("debutJour", debutJour)
                .getSingleResult();

        assertEquals(0L, count);
    }

    // ── countByStatut() ───────────────────────────────────────────

    @Test
    void countByStatut_shouldCountCorrectlyByStatut() {
        em.getTransaction().begin();
        Pays maroc  = buildAndPersistPays("MAR", "Maroc");
        Pays france = buildAndPersistPays("FRA", "France");
        Devise mad  = buildAndPersistDevise("MAD");
        Devise eur  = buildAndPersistDevise("EUR");
        Agence agence     = buildAndPersistAgence("Agence Test", "Rue 1", maroc, StatutAgence.ACTIVE);
        Corridor corridor = buildAndPersistCorridor(maroc, france, mad, eur);
        User agent        = buildAndPersistAgent("agent@okane.com", agence);
        Client expediteur  = buildAndPersistClient("ID001", "exp@okane.com", maroc);
        Client beneficiaire = buildAndPersistClient("ID002", "ben@okane.com", france);

        buildAndPersistTransfert("CODE001", new BigDecimal("500.00"), new BigDecimal("490.00"),
                StatutTransfert.EN_ATTENTE, LocalDateTime.now(), null,
                agence, null, corridor, expediteur, beneficiaire, agent);

        buildAndPersistTransfert("CODE002", new BigDecimal("300.00"), new BigDecimal("290.00"),
                StatutTransfert.PAYE, LocalDateTime.now(), LocalDateTime.now(),
                agence, agence, corridor, expediteur, beneficiaire, agent);

        buildAndPersistTransfert("CODE003", new BigDecimal("200.00"), new BigDecimal("190.00"),
                StatutTransfert.ANNULE, LocalDateTime.now(), null,
                agence, null, corridor, expediteur, beneficiaire, agent);

        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        Long countEnAttente = em.createQuery("""
                SELECT COUNT(t) FROM Transfert t
                WHERE t.agenceEnvoi.id = :agenceId AND t.statut = :statut
                """, Long.class)
                .setParameter("agenceId", agenceId)
                .setParameter("statut", StatutTransfert.EN_ATTENTE)
                .getSingleResult();

        Long countPaye = em.createQuery("""
                SELECT COUNT(t) FROM Transfert t
                WHERE t.agenceEnvoi.id = :agenceId AND t.statut = :statut
                """, Long.class)
                .setParameter("agenceId", agenceId)
                .setParameter("statut", StatutTransfert.PAYE)
                .getSingleResult();

        Long countAnnule = em.createQuery("""
                SELECT COUNT(t) FROM Transfert t
                WHERE t.agenceEnvoi.id = :agenceId AND t.statut = :statut
                """, Long.class)
                .setParameter("agenceId", agenceId)
                .setParameter("statut", StatutTransfert.ANNULE)
                .getSingleResult();

        assertEquals(1L, countEnAttente);
        assertEquals(1L, countPaye);
        assertEquals(1L, countAnnule);
    }

    // ── sumCommissionsGenerees() — CORRIGÉ ────────────────────────

    @Test
    void sumCommissionsGenerees_shouldSumPartAgenceForPayeTransferts() {
        em.getTransaction().begin();
        Pays maroc  = buildAndPersistPays("MAR", "Maroc");
        Pays france = buildAndPersistPays("FRA", "France");
        Devise mad  = buildAndPersistDevise("MAD");
        Devise eur  = buildAndPersistDevise("EUR");
        Agence agence     = buildAndPersistAgence("Agence Test", "Rue 1", maroc, StatutAgence.ACTIVE);
        Corridor corridor = buildAndPersistCorridor(maroc, france, mad, eur);
        User agent        = buildAndPersistAgent("agent@okane.com", agence);
        Client expediteur  = buildAndPersistClient("ID001", "exp@okane.com", maroc);
        Client beneficiaire = buildAndPersistClient("ID002", "ben@okane.com", france);

        buildAndPersistGrille(corridor,
                new BigDecimal("100.00"), new BigDecimal("2000.00"), 50.0);

        buildAndPersistTransfert("CODE001", new BigDecimal("500.00"), new BigDecimal("490.00"),
                StatutTransfert.PAYE, LocalDateTime.now(), LocalDateTime.now(),
                agence, agence, corridor, expediteur, beneficiaire, agent);

        buildAndPersistTransfert("CODE002", new BigDecimal("300.00"), new BigDecimal("290.00"),
                StatutTransfert.EN_ATTENTE, LocalDateTime.now(), null,
                agence, null, corridor, expediteur, beneficiaire, agent);

        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        // Double car partAgence est un double Java — H2 retourne Double pas BigDecimal
        Double sumRaw = em.createQuery("""
                SELECT COALESCE(SUM(g.partAgence), 0.0)
                FROM Transfert t
                JOIN GrilleTarifaire g ON g.corridor.id = t.corridor.id
                WHERE t.agenceEnvoi.id = :agenceId
                  AND t.statut = com.okane.entity.enums.StatutTransfert.PAYE
                  AND t.montantEnvoye BETWEEN g.montantMin AND g.montantMax
                """, Double.class)
                .setParameter("agenceId", agenceId)
                .getSingleResult();

        BigDecimal sum = BigDecimal.valueOf(sumRaw);
        assertEquals(0, new BigDecimal("50.0").compareTo(sum));
    }

    @Test
    void sumCommissionsGenerees_shouldReturnZeroWhenNoPayeTransferts() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays("MAR", "Maroc");
        Agence agence = buildAndPersistAgence("Agence Vide", "Rue 1", pays, StatutAgence.ACTIVE);
        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        // Double car partAgence est un double Java
        Double sumRaw = em.createQuery("""
                SELECT COALESCE(SUM(g.partAgence), 0.0)
                FROM Transfert t
                JOIN GrilleTarifaire g ON g.corridor.id = t.corridor.id
                WHERE t.agenceEnvoi.id = :agenceId
                  AND t.statut = com.okane.entity.enums.StatutTransfert.PAYE
                  AND t.montantEnvoye BETWEEN g.montantMin AND g.montantMax
                """, Double.class)
                .setParameter("agenceId", agenceId)
                .getSingleResult();

        BigDecimal sum = BigDecimal.valueOf(sumRaw);
        assertEquals(0, BigDecimal.ZERO.compareTo(sum));
    }

    @Test
    void sumCommissionsGenerees_shouldReturnZeroWhenMontantOutsideGrille() {
        em.getTransaction().begin();
        Pays maroc  = buildAndPersistPays("MAR", "Maroc");
        Pays france = buildAndPersistPays("FRA", "France");
        Devise mad  = buildAndPersistDevise("MAD");
        Devise eur  = buildAndPersistDevise("EUR");
        Agence agence     = buildAndPersistAgence("Agence Test", "Rue 1", maroc, StatutAgence.ACTIVE);
        Corridor corridor = buildAndPersistCorridor(maroc, france, mad, eur);
        User agent        = buildAndPersistAgent("agent@okane.com", agence);
        Client expediteur  = buildAndPersistClient("ID001", "exp@okane.com", maroc);
        Client beneficiaire = buildAndPersistClient("ID002", "ben@okane.com", france);

        buildAndPersistGrille(corridor,
                new BigDecimal("100.00"), new BigDecimal("500.00"), 30.0);

        buildAndPersistTransfert("CODE001", new BigDecimal("1000.00"), new BigDecimal("990.00"),
                StatutTransfert.PAYE, LocalDateTime.now(), LocalDateTime.now(),
                agence, agence, corridor, expediteur, beneficiaire, agent);

        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        //  Double car partAgence est un double Java
        Double sumRaw = em.createQuery("""
                SELECT COALESCE(SUM(g.partAgence), 0.0)
                FROM Transfert t
                JOIN GrilleTarifaire g ON g.corridor.id = t.corridor.id
                WHERE t.agenceEnvoi.id = :agenceId
                  AND t.statut = com.okane.entity.enums.StatutTransfert.PAYE
                  AND t.montantEnvoye BETWEEN g.montantMin AND g.montantMax
                """, Double.class)
                .setParameter("agenceId", agenceId)
                .getSingleResult();

        BigDecimal sum = BigDecimal.valueOf(sumRaw);
        assertEquals(0, BigDecimal.ZERO.compareTo(sum));
    }
}