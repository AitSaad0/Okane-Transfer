package com.okane.service.impl;

import com.okane.dto.requestDto.AuthRequestDTO;
import com.okane.dto.requestDto.RefreshRequestDTO;
import com.okane.dto.requestDto.RegisterRequestDTO;
import com.okane.dto.responseDto.AuthResponseDTO;
import com.okane.dto.responseDto.UserResponseDTO;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import com.okane.exception.BadCredentialsException;
import com.okane.exception.InvalidTokenException;
import com.okane.exception.UnauthorizedAccessException;
import com.okane.exception.UserAlreadyExistsException;
import com.okane.repository.UserRepository;
import com.okane.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil        jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail()))
            throw new UserAlreadyExistsException(
                    "Email already in use: " + dto.getEmail()
            );

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .telephone(dto.getTelephone())
                .role(Role.CLIENT)
                .active(true)
                .build();

        userRepository.save(user);
        return buildTokens(user);
    }


    @Transactional(readOnly = true)
    public AuthResponseDTO login(AuthRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            throw new BadCredentialsException("Invalid email or password");

        if (!user.isEnabled())
            throw new BadCredentialsException("Account is disabled");

        return buildTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO refresh(RefreshRequestDTO dto) {
        String token = dto.getRefreshToken();

        if (!jwtUtil.isRefreshToken(token))
            throw new InvalidTokenException("Invalid or expired refresh token");

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User not found for this token"));

        if (!user.isEnabled())
            throw new BadCredentialsException("Account is disabled");

        return buildTokens(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .telephone(user.getTelephone())
                .role(user.getRole())
                .active(user.getActive())
                .build();
    }

    private AuthResponseDTO buildTokens(User user) {
        return AuthResponseDTO.builder()
                .accessToken(jwtUtil.generateAccessToken(user.getEmail()))
                .refreshToken(jwtUtil.generateRefreshToken(user.getEmail()))
                .build();
    }
}