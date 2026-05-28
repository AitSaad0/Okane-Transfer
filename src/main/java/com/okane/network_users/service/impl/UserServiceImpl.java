package com.okane.network_users.service.impl;

import com.okane.network_users.bean.Agence;
import com.okane.network_users.bean.User;
import com.okane.network_users.controller.converter.UserConverter;
import com.okane.network_users.controller.dto.requestDto.CreateUserRequestDto;
import com.okane.network_users.controller.dto.requestDto.UpdateUserRequestDto;
import com.okane.network_users.controller.dto.requestDto.UpdateUserStatusRequestDto;
import com.okane.pagination.PageResponseDto;
import com.okane.network_users.controller.dto.responseDto.UserResponseDto;
import com.okane.network_users.repository.AgenceRepository;
import com.okane.network_users.repository.UserRepository;
import com.okane.network_users.service.facade.UserService;
import com.okane.shared.Role;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND = "Utilisateur introuvable avec l'id : ";

    private final UserRepository   userRepository;
    private final AgenceRepository agenceRepository;
    private final UserConverter    userConverter;
    private final PasswordEncoder  passwordEncoder;

    // GET /api/v1/admin/users ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<UserResponseDto> getAllUsers(Role role, Boolean active, Long agenceId,
                                                        int page, int size, String sort) {
        Sort sortOrder = Sort.by(Sort.Direction.ASC, sort != null ? sort : "id");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<User> usersPage = userRepository.findAllWithFilters(role, active, agenceId, pageable);

        return new PageResponseDto<>(
                usersPage.getContent().stream().map(userConverter::toResponseDto).toList(),
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast()
        );
    }

    // ── GET /api/v1/admin/users/{id} ──────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = findActiveUserOrThrow(id);
        return userConverter.toResponseDto(user);
    }

    // ── POST /api/v1/admin/users ──────────────────────────────────────────────
    @Override
    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
        }

        Agence agence = resolveAgence(request.getAgenceId());

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .telephone(request.getTelephone())
                .role(request.getRole())
                .active(true)
                .deleted(false)
                .agence(agence)
                .build();

        return userConverter.toResponseDto(userRepository.save(user));
    }

    // ── PUT /api/v1/admin/users/{id} ──────────────────────────────────────────
    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UpdateUserRequestDto request) {
        User user = findActiveUserOrThrow(id);

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());
        user.setAgence(resolveAgence(request.getAgenceId()));

        return userConverter.toResponseDto(userRepository.save(user));
    }

    // ── PATCH /api/v1/admin/users/{id}/status ─────────────────────────────────
    @Override
    @Transactional
    public UserResponseDto updateUserStatus(Long id, UpdateUserStatusRequestDto request) {
        User user = findActiveUserOrThrow(id);
        user.setActive(request.getActive());
        return userConverter.toResponseDto(userRepository.save(user));
    }

    // ── DELETE /api/v1/admin/users/{id} — Soft-delete RGPD ───────────────────
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findActiveUserOrThrow(id);

        String anonymToken = UUID.randomUUID().toString();

        user.setEmail("deleted_" + anonymToken + "@anonymized.local");
        user.setNom("ANONYMIZED");
        user.setPrenom("ANONYMIZED");
        user.setTelephone(null);
        user.setTwoFactorSecret(null);
        user.setActive(false);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private User findActiveUserOrThrow(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + id));
    }

    private Agence resolveAgence(Long agenceId) {
        if (agenceId == null) return null;
        return agenceRepository.findById(agenceId)
                .orElseThrow(() -> new EntityNotFoundException("Agence introuvable avec l'id : " + agenceId));
    }
}