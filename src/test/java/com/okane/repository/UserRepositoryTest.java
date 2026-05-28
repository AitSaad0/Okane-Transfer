package com.okane.repository;

import com.okane.entity.User;
import com.okane.entity.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

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
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @AfterAll
    static void closeFactory() {
        emf.close();
    }

    private User buildUser(String email) {
        return User.builder()
                .email(email)
                .password("encodedPassword")
                .nom("Doe")
                .prenom("John")
                .role(Role.CLIENT)
                .active(true)
                .build();
    }

    private Optional<User> findByEmail(String email) {
        return em.createQuery("FROM User WHERE email = :email", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Test
    void save_shouldPersistUser() {
        em.getTransaction().begin();
        em.persist(buildUser("client@okane.com"));
        em.getTransaction().commit();
        em.clear();

        assertNotNull(findByEmail("client@okane.com"));
    }

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
}