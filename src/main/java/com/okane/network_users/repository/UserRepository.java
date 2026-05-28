package com.okane.network_users.repository;

import com.okane.network_users.bean.User;
import com.okane.shared.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Utilisé pour l'auth — exclut les supprimés
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

    

    Optional<User> findByEmail(String email);

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


    boolean existsByIdAndAgenceId(Long userId, Long agenceId);
}
