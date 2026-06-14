package com.okane.repository;

import com.okane.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.okane.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedFalse(String email);
    boolean existsByEmail(String email);

    // Liste paginée avec filtres optionnels
    @Query("""
            SELECT u FROM User u
            WHERE u.deleted = false
              AND (:role     IS NULL OR u.role        = :role)
              AND (:active   IS NULL OR u.active      = :active)
              AND (:agenceId IS NULL OR u.agence.id   = :agenceId)
            """)
    Page<User> findAllWithFilters(
            @Param("role")     Role    role,
            @Param("active")   Boolean active,
            @Param("agenceId") Long    agenceId,
            Pageable pageable
    );

    // Détail d'un utilisateur non supprimé
    Optional<User> findByIdAndDeletedFalse(Long id);


    Page<User> findByDeletedFalse(Pageable pageable);
    Page<User> findByRoleAndDeletedFalse(Role role, Pageable pageable);
    Page<User> findByActiveAndDeletedFalse(Boolean active, Pageable pageable);
    Page<User> findByAgenceIdAndDeletedFalse(Long agenceId, Pageable pageable);
    Page<User> findByRoleAndActiveAndDeletedFalse(Role role, Boolean active, Pageable pageable);
    Page<User> findByRoleAndAgenceIdAndDeletedFalse(Role role, Long agenceId, Pageable pageable);
    Page<User> findByActiveAndAgenceIdAndDeletedFalse(Boolean active, Long agenceId, Pageable pageable);
    Page<User> findByRoleAndActiveAndAgenceIdAndDeletedFalse(Role role, Boolean active, Long agenceId, Pageable pageable);

    // Pour gestion agents par agence
    List<User> findByAgenceIdAndRoleAndDeletedFalse(Long agenceId, Role role);
    List<User> findByDeletedFalse();

    boolean existsByIdAndAgenceId(Long userId, Long agenceId);
}