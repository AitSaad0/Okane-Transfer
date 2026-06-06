package com.okane.repository;

import com.okane.entity.Agence;
import com.okane.entity.Pays;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import com.okane.entity.enums.StatutAgence;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

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

    // ─────────────────────────────────────────────────────────────
    // Helpers — builders
    // ─────────────────────────────────────────────────────────────

    private Pays buildAndPersistPays() {
        Pays pays = Pays.builder()
                .codeIso("MAR")
                .nom("Maroc")
                .build();
        em.persist(pays);
        return pays;
    }

    private Agence buildAndPersistAgence(Pays pays) {
        Agence agence = Agence.builder()
                .nom("Agence Casablanca")
                .adresse("123 Rue Hassan II")
                .plafondJournalier(new BigDecimal("50000.00"))
                .statut(StatutAgence.ACTIVE)
                .ville("Casablanca")
                .codePostal("20000")
                .pays(pays)
                .build();
        em.persist(agence);
        return agence;
    }

    private User buildUser(String email) {
        return User.builder()
                .email(email)
                .password("encodedPassword")
                .nom("Doe")
                .prenom("John")
                .role(Role.CLIENT)
                .active(true)
                .deleted(false)
                .build();
    }

    private User buildUserWithRole(String email, Role role) {
        return User.builder()
                .email(email)
                .password("encodedPassword")
                .nom("Doe")
                .prenom("John")
                .role(role)
                .active(true)
                .deleted(false)
                .build();
    }

    private User buildUserWithAgence(String email, Role role, Agence agence) {
        return User.builder()
                .email(email)
                .password("encodedPassword")
                .nom("Doe")
                .prenom("John")
                .role(role)
                .active(true)
                .deleted(false)
                .agence(agence)
                .build();
    }

    private User buildDeletedUser(String email) {
        return User.builder()
                .email(email)
                .password("encodedPassword")
                .nom("Deleted")
                .prenom("User")
                .role(Role.CLIENT)
                .active(true)
                .deleted(true)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers — queries (simulent les méthodes du repository)
    // ─────────────────────────────────────────────────────────────

    private Optional<User> findByEmail(String email) {
        return em.createQuery("FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    private Optional<User> findByEmailAndDeletedFalse(String email) {
        return em.createQuery(
                        "FROM User u WHERE u.email = :email AND u.deleted = false", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    private Optional<User> findByIdAndDeletedFalse(Long id) {
        return em.createQuery(
                        "FROM User u WHERE u.id = :id AND u.deleted = false", User.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    private boolean existsByEmail(String email) {
        Long count = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    private boolean existsByIdAndAgenceId(Long userId, Long agenceId) {
        Long count = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.id = :id AND u.agence.id = :agenceId", Long.class)
                .setParameter("id", userId)
                .setParameter("agenceId", agenceId)
                .getSingleResult();
        return count > 0;
    }

    private long countByDeletedFalse() {
        return em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.deleted = false", Long.class)
                .getSingleResult();
    }

    private long countByRoleAndDeletedFalse(Role role) {
        return em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.deleted = false", Long.class)
                .setParameter("role", role)
                .getSingleResult();
    }

    private long countByActiveAndDeletedFalse(Boolean active) {
        return em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.active = :active AND u.deleted = false", Long.class)
                .setParameter("active", active)
                .getSingleResult();
    }

    private long countByAgenceIdAndDeletedFalse(Long agenceId) {
        return em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.agence.id = :agenceId AND u.deleted = false", Long.class)
                .setParameter("agenceId", agenceId)
                .getSingleResult();
    }

    private long countByAgenceIdAndRoleAndDeletedFalse(Long agenceId, Role role) {
        return em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.agence.id = :agenceId AND u.role = :role AND u.deleted = false", Long.class)
                .setParameter("agenceId", agenceId)
                .setParameter("role", role)
                .getSingleResult();
    }

    // ─────────────────────────────────────────────────────────────
    // save / persist
    // ─────────────────────────────────────────────────────────────

    @Test
    void save_shouldPersistUser() {
        em.getTransaction().begin();
        em.persist(buildUser("client@okane.com"));
        em.getTransaction().commit();
        em.clear();

        assertTrue(findByEmail("client@okane.com").isPresent());
    }

    @Test
    void save_shouldPersistAllFieldsCorrectly() {
        User user = User.builder()
                .email("full@okane.com")
                .password("encodedPassword")
                .nom("Doe")
                .prenom("John")
                .telephone("0612345678")
                .role(Role.CLIENT)
                .active(true)
                .deleted(false)
                .notificationEmail(true)
                .notificationSms(false)
                .notificationPush(true)
                .build();

        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        em.clear();

        User found = findByEmail("full@okane.com").orElseThrow();

        assertEquals("full@okane.com", found.getEmail());
        assertEquals("Doe",            found.getNom());
        assertEquals("John",           found.getPrenom());
        assertEquals("0612345678",     found.getTelephone());
        assertEquals(Role.CLIENT,      found.getRole());
        assertTrue(found.getActive());
        assertFalse(found.getDeleted());
        assertTrue(found.getNotificationEmail());
        assertFalse(found.getNotificationSms());
        assertTrue(found.getNotificationPush());
    }

    @Test
    void save_shouldPersistDisabledUserCorrectly() {
        User user = User.builder()
                .email("disabled@okane.com")
                .password("encodedPassword")
                .nom("Doe")
                .prenom("Jane")
                .role(Role.CLIENT)
                .active(false)
                .deleted(false)
                .build();

        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        em.clear();

        User found = findByEmail("disabled@okane.com").orElseThrow();
        assertFalse(found.getActive());
    }

    @Test
    void save_shouldPersistDeletedFlagCorrectly() {
        em.getTransaction().begin();
        em.persist(buildDeletedUser("softdeleted@okane.com"));
        em.getTransaction().commit();
        em.clear();

        User found = findByEmail("softdeleted@okane.com").orElseThrow();
        assertTrue(found.getDeleted());
    }

    @Test
    void save_shouldPersistUserWithAgenceCorrectly() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        Agence agence = buildAndPersistAgence(pays);
        User user = buildUserWithAgence("agent@okane.com", Role.AGENT, agence);
        em.persist(user);
        em.getTransaction().commit();
        em.clear();

        User found = findByEmail("agent@okane.com").orElseThrow();
        assertNotNull(found.getAgence());
        assertEquals(agence.getId(), found.getAgence().getId());
    }

    // ─────────────────────────────────────────────────────────────
    // findByEmail()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findByEmail_shouldReturnUserWhenExists() {
        em.getTransaction().begin();
        em.persist(buildUser("client@okane.com"));
        em.getTransaction().commit();
        em.clear();

        Optional<User> result = findByEmail("client@okane.com");

        assertTrue(result.isPresent());
        assertEquals("client@okane.com", result.get().getEmail());
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenNotExists() {
        Optional<User> result = findByEmail("nobody@okane.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmail_shouldReturnDeletedUserToo() {
        em.getTransaction().begin();
        em.persist(buildDeletedUser("deleted@okane.com"));
        em.getTransaction().commit();
        em.clear();

        // findByEmail ne filtre PAS deleted — contrairement à findByEmailAndDeletedFalse
        Optional<User> result = findByEmail("deleted@okane.com");
        assertTrue(result.isPresent());
    }

    // ─────────────────────────────────────────────────────────────
    // findByEmailAndDeletedFalse()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findByEmailAndDeletedFalse_shouldReturnUserWhenNotDeleted() {
        em.getTransaction().begin();
        em.persist(buildUser("active@okane.com"));
        em.getTransaction().commit();
        em.clear();

        Optional<User> result = findByEmailAndDeletedFalse("active@okane.com");

        assertTrue(result.isPresent());
        assertEquals("active@okane.com", result.get().getEmail());
    }

    @Test
    void findByEmailAndDeletedFalse_shouldReturnEmptyWhenDeleted() {
        em.getTransaction().begin();
        em.persist(buildDeletedUser("deleted@okane.com"));
        em.getTransaction().commit();
        em.clear();

        Optional<User> result = findByEmailAndDeletedFalse("deleted@okane.com");
        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────
    // existsByEmail()
    // ─────────────────────────────────────────────────────────────

    @Test
    void existsByEmail_shouldReturnTrueWhenUserExists() {
        em.getTransaction().begin();
        em.persist(buildUser("client@okane.com"));
        em.getTransaction().commit();
        em.clear();

        assertTrue(existsByEmail("client@okane.com"));
    }

    @Test
    void existsByEmail_shouldReturnFalseWhenUserDoesNotExist() {
        assertFalse(existsByEmail("nobody@okane.com"));
    }

    @Test
    void existsByEmail_shouldReturnTrueEvenForDeletedUser() {
        em.getTransaction().begin();
        em.persist(buildDeletedUser("deleted@okane.com"));
        em.getTransaction().commit();
        em.clear();

        // existsByEmail ne filtre PAS deleted
        assertTrue(existsByEmail("deleted@okane.com"));
    }

    // ─────────────────────────────────────────────────────────────
    // findByIdAndDeletedFalse()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findByIdAndDeletedFalse_shouldReturnUserWhenNotDeleted() {
        em.getTransaction().begin();
        User user = buildUser("findbyid@okane.com");
        em.persist(user);
        em.getTransaction().commit();
        Long id = user.getId();
        em.clear();

        Optional<User> result = findByIdAndDeletedFalse(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findByIdAndDeletedFalse_shouldReturnEmptyWhenDeleted() {
        em.getTransaction().begin();
        User user = buildDeletedUser("deletedbyid@okane.com");
        em.persist(user);
        em.getTransaction().commit();
        Long id = user.getId();
        em.clear();

        Optional<User> result = findByIdAndDeletedFalse(id);
        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────
    // findByDeletedFalse()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findByDeletedFalse_shouldReturnOnlyNonDeletedUsers() {
        em.getTransaction().begin();
        em.persist(buildUser("active1@okane.com"));
        em.persist(buildUser("active2@okane.com"));
        em.persist(buildDeletedUser("deleted@okane.com"));
        em.getTransaction().commit();
        em.clear();

        assertEquals(2L, countByDeletedFalse());
    }

    @Test
    void findByDeletedFalse_shouldReturnEmptyWhenAllDeleted() {
        em.getTransaction().begin();
        em.persist(buildDeletedUser("deleted1@okane.com"));
        em.persist(buildDeletedUser("deleted2@okane.com"));
        em.getTransaction().commit();
        em.clear();

        assertEquals(0L, countByDeletedFalse());
    }

    // ─────────────────────────────────────────────────────────────
    // findByRoleAndDeletedFalse()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findByRoleAndDeletedFalse_shouldReturnUsersMatchingRole() {
        em.getTransaction().begin();
        em.persist(buildUserWithRole("agent@okane.com",  Role.AGENT));
        em.persist(buildUserWithRole("client@okane.com", Role.CLIENT));
        em.persist(buildUserWithRole("admin@okane.com",  Role.ADMIN));
        em.getTransaction().commit();
        em.clear();

        assertEquals(1L, countByRoleAndDeletedFalse(Role.AGENT));
        assertEquals(1L, countByRoleAndDeletedFalse(Role.CLIENT));
        assertEquals(1L, countByRoleAndDeletedFalse(Role.ADMIN));
    }

    @Test
    void findByRoleAndDeletedFalse_shouldExcludeDeletedUsersWithSameRole() {
        em.getTransaction().begin();
        em.persist(buildUserWithRole("agent@okane.com", Role.AGENT));
        // deleted AGENT — ne doit pas apparaître
        User deletedAgent = buildDeletedUser("deletedagent@okane.com");
        deletedAgent.setRole(Role.AGENT);
        em.persist(deletedAgent);
        em.getTransaction().commit();
        em.clear();

        assertEquals(1L, countByRoleAndDeletedFalse(Role.AGENT));
    }

    // ─────────────────────────────────────────────────────────────
    // findByActiveAndDeletedFalse()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findByActiveAndDeletedFalse_shouldReturnOnlyActiveNonDeletedUsers() {
        em.getTransaction().begin();
        em.persist(buildUser("active@okane.com")); // active=true, deleted=false

        User inactive = User.builder()
                .email("inactive@okane.com")
                .password("encodedPassword")
                .nom("Doe").prenom("Jane")
                .role(Role.CLIENT)
                .active(false)
                .deleted(false)
                .build();
        em.persist(inactive);
        em.getTransaction().commit();
        em.clear();

        assertEquals(1L, countByActiveAndDeletedFalse(true));
        assertEquals(1L, countByActiveAndDeletedFalse(false));
    }

    @Test
    void findByActiveAndDeletedFalse_shouldExcludeDeletedUsers() {
        em.getTransaction().begin();
        em.persist(buildUser("active@okane.com"));       // active=true, deleted=false
        em.persist(buildDeletedUser("deleted@okane.com")); // active=true, deleted=true
        em.getTransaction().commit();
        em.clear();

        // Le deleted=true ne doit pas compter même si active=true
        assertEquals(1L, countByActiveAndDeletedFalse(true));
    }

    // ─────────────────────────────────────────────────────────────
    // findByAgenceIdAndDeletedFalse()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findByAgenceIdAndDeletedFalse_shouldReturnUsersOfAgence() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        Agence agence = buildAndPersistAgence(pays);
        em.persist(buildUserWithAgence("agent1@okane.com", Role.AGENT, agence));
        em.persist(buildUserWithAgence("agent2@okane.com", Role.AGENT, agence));
        em.persist(buildUser("noagence@okane.com")); // sans agence
        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        assertEquals(2L, countByAgenceIdAndDeletedFalse(agenceId));
    }

    @Test
    void findByAgenceIdAndDeletedFalse_shouldExcludeDeletedUsers() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        Agence agence = buildAndPersistAgence(pays);
        em.persist(buildUserWithAgence("agent@okane.com", Role.AGENT, agence));

        User deletedAgent = buildDeletedUser("deletedagent@okane.com");
        deletedAgent.setAgence(agence);
        em.persist(deletedAgent);
        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        assertEquals(1L, countByAgenceIdAndDeletedFalse(agenceId));
    }

    // ─────────────────────────────────────────────────────────────
    // findByAgenceIdAndRoleAndDeletedFalse()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findByAgenceIdAndRoleAndDeletedFalse_shouldReturnAgentsOfAgence() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        Agence agence = buildAndPersistAgence(pays);
        em.persist(buildUserWithAgence("agent@okane.com",  Role.AGENT,  agence));
        em.persist(buildUserWithAgence("client@okane.com", Role.CLIENT, agence));
        em.getTransaction().commit();
        Long agenceId = agence.getId();
        em.clear();

        assertEquals(1L, countByAgenceIdAndRoleAndDeletedFalse(agenceId, Role.AGENT));
        assertEquals(1L, countByAgenceIdAndRoleAndDeletedFalse(agenceId, Role.CLIENT));
    }

    // ─────────────────────────────────────────────────────────────
    // existsByIdAndAgenceId()
    // ─────────────────────────────────────────────────────────────

    @Test
    void existsByIdAndAgenceId_shouldReturnTrueWhenUserBelongsToAgence() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        Agence agence = buildAndPersistAgence(pays);
        User user = buildUserWithAgence("agent@okane.com", Role.AGENT, agence);
        em.persist(user);
        em.getTransaction().commit();
        Long userId   = user.getId();
        Long agenceId = agence.getId();
        em.clear();

        assertTrue(existsByIdAndAgenceId(userId, agenceId));
    }

    @Test
    void existsByIdAndAgenceId_shouldReturnFalseWhenAgenceMismatch() {
        em.getTransaction().begin();
        Pays pays = buildAndPersistPays();
        Agence agence = buildAndPersistAgence(pays);
        User user = buildUserWithAgence("agent@okane.com", Role.AGENT, agence);
        em.persist(user);
        em.getTransaction().commit();
        Long userId = user.getId();
        em.clear();

        assertFalse(existsByIdAndAgenceId(userId, 999L));
    }

    @Test
    void existsByIdAndAgenceId_shouldReturnFalseWhenUserHasNoAgence() {
        em.getTransaction().begin();
        User user = buildUser("noagence@okane.com");
        em.persist(user);
        em.getTransaction().commit();
        Long userId = user.getId();
        em.clear();

        assertFalse(existsByIdAndAgenceId(userId, 1L));
    }
}