package com.okane.repository;

import com.okane.entity.Client;
import com.okane.entity.Pays;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClientRepositoryTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    // ─────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────

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
        em.createQuery("DELETE FROM Client").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.createQuery("DELETE FROM Pays").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @AfterAll
    static void closeFactory() {
        emf.close();
    }

    // ─────────────────────────────────────────────────────────────
    // Builders
    // ─────────────────────────────────────────────────────────────

    private Pays buildAndPersistPays() {
        Pays pays = Pays.builder()
                .codeIso("MAR")
                .nom("Maroc")
                .build();
        em.persist(pays);
        return pays;
    }

    private User buildAndPersistUser(String email) {
        User user = User.builder()
                .email(email)
                .password("encodedPassword")
                .nom("Doe")
                .prenom("John")
                .role(Role.CLIENT)
                .active(true)
                .deleted(false)
                .build();
        em.persist(user);
        return user;
    }

    private Client buildAndPersistClient(String numPiece, String telephone,
                                         String email, Pays pays, User user) {
        Client client = Client.builder()
                .nom("Doe")
                .prenom("Jane")
                .numPieceIdentite(numPiece)
                .telephone(telephone)
                .email(email)
                .estSurListeSurveillance(false)
                .deleted(false)
                .pays(pays)
                .user(user)
                .build();
        em.persist(client);
        return client;
    }

    private Client buildAndPersistClientWithoutUser(String numPiece, String telephone,
                                                    String email, Pays pays) {
        Client client = Client.builder()
                .nom("Doe")
                .prenom("Jane")
                .numPieceIdentite(numPiece)
                .telephone(telephone)
                .email(email)
                .estSurListeSurveillance(false)
                .deleted(false)
                .pays(pays)
                .build();
        em.persist(client);
        return client;
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers — queries
    // ─────────────────────────────────────────────────────────────

    private Optional<Client> findByUserId(Long userId) {
        return em.createQuery(
                        "FROM Client c WHERE c.user.id = :userId", Client.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }

    private boolean existsByNumPieceIdentite(String numPiece) {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM Client c WHERE c.numPieceIdentite = :num", Long.class)
                .setParameter("num", numPiece)
                .getSingleResult();
        return count > 0;
    }

    private boolean existsByTelephone(String telephone) {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM Client c WHERE c.telephone = :tel", Long.class)
                .setParameter("tel", telephone)
                .getSingleResult();
        return count > 0;
    }

    // ─────────────────────────────────────────────────────────────
    // save / persist
    // ─────────────────────────────────────────────────────────────

    @Test
    void save_shouldPersistClientWithAllFields() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        User user = buildAndPersistUser("user@okane.com");
        Client client = buildAndPersistClient("ID001", "0600000001", "client@okane.com", pays, user);
        em.getTransaction().commit();
        em.clear();

        Client found = em.find(Client.class, client.getId());

        assertNotNull(found);
        assertEquals("ID001",           found.getNumPieceIdentite());
        assertEquals("0600000001",      found.getTelephone());
        assertEquals("client@okane.com", found.getEmail());
        assertEquals("Doe",             found.getNom());
        assertEquals("Jane",            found.getPrenom());
        assertFalse(found.getEstSurListeSurveillance());
        assertFalse(found.getDeleted());
    }

    @Test
    void save_shouldPersistClientWithoutUser() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        Client client = buildAndPersistClientWithoutUser("ID001", "0600000001", "client@okane.com", pays);
        em.getTransaction().commit();
        em.clear();

        Client found = em.find(Client.class, client.getId());

        assertNotNull(found);
        assertNull(found.getUser());
    }

    // ─────────────────────────────────────────────────────────────
    // findByUserId()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findByUserId_shouldReturnClientWhenUserLinked() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        User user = buildAndPersistUser("user@okane.com");
        buildAndPersistClient("ID001", "0600000001", "client@okane.com", pays, user);
        em.getTransaction().commit();
        Long userId = user.getId();
        em.clear();

        Optional<Client> result = findByUserId(userId);

        assertTrue(result.isPresent());
        assertEquals("ID001", result.get().getNumPieceIdentite());
    }

    @Test
    void findByUserId_shouldReturnEmptyWhenNoClientLinkedToUser() {
        em.getTransaction().begin();
        User user = buildAndPersistUser("user@okane.com");
        em.getTransaction().commit();
        Long userId = user.getId();
        em.clear();

        Optional<Client> result = findByUserId(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserId_shouldReturnEmptyForUnknownUserId() {
        Optional<Client> result = findByUserId(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserId_shouldNotReturnClientWithoutUser() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        buildAndPersistClientWithoutUser("ID001", "0600000001", "client@okane.com", pays);
        em.getTransaction().commit();
        em.clear();

        Optional<Client> result = findByUserId(999L);

        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────
    // existsByNumPieceIdentite()
    // ─────────────────────────────────────────────────────────────

    @Test
    void existsByNumPieceIdentite_shouldReturnTrueWhenExists() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        buildAndPersistClientWithoutUser("ID001", "0600000001", "client@okane.com", pays);
        em.getTransaction().commit();
        em.clear();

        assertTrue(existsByNumPieceIdentite("ID001"));
    }

    @Test
    void existsByNumPieceIdentite_shouldReturnFalseWhenNotExists() {
        assertFalse(existsByNumPieceIdentite("IDXXX"));
    }

    @Test
    void existsByNumPieceIdentite_shouldReturnFalseForDifferentNum() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        buildAndPersistClientWithoutUser("ID001", "0600000001", "client@okane.com", pays);
        em.getTransaction().commit();
        em.clear();

        assertFalse(existsByNumPieceIdentite("ID002"));
    }

    @Test
    void existsByNumPieceIdentite_shouldReturnTrueForDeletedClient() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        Client client = buildAndPersistClientWithoutUser("ID001", "0600000001", "client@okane.com", pays);
        client.setDeleted(true);
        em.merge(client);
        em.getTransaction().commit();
        em.clear();

        // existsByNumPieceIdentite ne filtre PAS deleted
        assertTrue(existsByNumPieceIdentite("ID001"));
    }

    // ─────────────────────────────────────────────────────────────
    // existsByTelephone()
    // ─────────────────────────────────────────────────────────────

    @Test
    void existsByTelephone_shouldReturnTrueWhenExists() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        buildAndPersistClientWithoutUser("ID001", "0600000001", "client@okane.com", pays);
        em.getTransaction().commit();
        em.clear();

        assertTrue(existsByTelephone("0600000001"));
    }

    @Test
    void existsByTelephone_shouldReturnFalseWhenNotExists() {
        assertFalse(existsByTelephone("0699999999"));
    }

    @Test
    void existsByTelephone_shouldReturnFalseForDifferentTelephone() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        buildAndPersistClientWithoutUser("ID001", "0600000001", "client@okane.com", pays);
        em.getTransaction().commit();
        em.clear();

        assertFalse(existsByTelephone("0611111111"));
    }

    @Test
    void existsByTelephone_shouldDistinguishBetweenTwoClients() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        buildAndPersistClientWithoutUser("ID001", "0600000001", "client1@okane.com", pays);
        buildAndPersistClientWithoutUser("ID002", "0600000002", "client2@okane.com", pays);
        em.getTransaction().commit();
        em.clear();

        assertTrue(existsByTelephone("0600000001"));
        assertTrue(existsByTelephone("0600000002"));
        assertFalse(existsByTelephone("0600000003"));
    }
}