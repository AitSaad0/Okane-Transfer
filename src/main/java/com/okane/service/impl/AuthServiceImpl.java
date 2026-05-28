package com.okane.service.impl;

import com.okane.dto.requestDto.AuthRequestDTO;
import com.okane.dto.responseDto.AuthResponseDTO;
import com.okane.dto.requestDto.RegisterRequestDTO;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import com.okane.exception.ResourceNotFoundException;
import com.okane.exception.UnauthorizedAccessException;
import com.okane.repository.UserRepository;
import com.okane.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (dto.getRole() != Role.CLIENT)
            throw new UnauthorizedAccessException("Public registration is for clients only");

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
        return new AuthResponseDTO(jwtUtil.generateToken(user.getEmail()));
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(AuthRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            throw new ResourceNotFoundException("Invalid credentials");

        if (!user.isEnabled())
            throw new ResourceNotFoundException("Account is disabled");

        return new AuthResponseDTO(jwtUtil.generateToken(user.getEmail()));
    }
}