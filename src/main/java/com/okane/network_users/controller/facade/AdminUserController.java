package com.okane.network_users.controller.facade;

import com.okane.network_users.controller.dto.requestDto.CreateUserRequestDto;
import com.okane.network_users.controller.dto.requestDto.UpdateUserRequestDto;
import com.okane.network_users.controller.dto.requestDto.UpdateUserStatusRequestDto;
import com.okane.pagination.PageResponseDto;
import com.okane.network_users.controller.dto.responseDto.UserResponseDto;
import com.okane.network_users.service.facade.UserService;
import com.okane.shared.Role;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
//@Tag(name = "Admin — Utilisateurs", description = "CRUD complet des utilisateurs")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    //@Operation(summary = "Liste paginée de tous les utilisateurs")
    public ResponseEntity<PageResponseDto<UserResponseDto>> getAllUsers(
            @RequestParam(required = false) Role    role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long    agenceId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort
    ) {
        return ResponseEntity.ok(
                userService.getAllUsers(role, active, agenceId, page, size, sort)
        );
    }

    @GetMapping("/{id}")
    //@Operation(summary = "Détail complet d'un utilisateur")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    //@Operation(summary = "Crée un utilisateur avec rôle spécifié")
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody CreateUserRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    //@Operation(summary = "Met à jour les informations d'un utilisateur")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDto request
    ) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/{id}/status")
    //@Operation(summary = "Active ou suspend un compte utilisateur")
    public ResponseEntity<UserResponseDto> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequestDto request
    ) {
        return ResponseEntity.ok(userService.updateUserStatus(id, request));
    }

    @DeleteMapping("/{id}")
    //@Operation(summary = "Soft-delete RGPD d'un utilisateur")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}