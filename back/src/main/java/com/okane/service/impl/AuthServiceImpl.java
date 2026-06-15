package com.okane.service.impl;

import com.okane.dto.requestDto.*;
import com.okane.dto.responseDto.AuthResponseDTO;
import com.okane.dto.responseDto.UserResponseDTO;
import com.okane.entity.Token;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import com.okane.entity.enums.TypeToken;
import com.okane.exception.*;
import com.okane.repository.TokenRepository;
import com.okane.repository.UserRepository;
import com.okane.security.JwtUtil;
import com.okane.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthServiceImpl {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil        jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private TokenRepository tokenRepository;

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


    @Transactional
    public AuthResponseDTO login(AuthRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            throw new BadCredentialsException("Invalid email or password");

        if (!user.isEnabled())
            throw new BadCredentialsException("Account is disabled");

        return buildTokens(user);
    }

    @Transactional
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

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for: " + email));

        tokenRepository.deleteByUtilisateurIdAndType(user.getId(), TypeToken.PASSWORD_RESET);

        String code = String.format("%06d", new Random().nextInt(999999));

        Token token = Token.builder()
                .valeur(code)
                .type(TypeToken.PASSWORD_RESET)
                .dateExpiration(LocalDateTime.now().plusMinutes(15))
                .booleenUtilise(false)
                .utilisateur(user)
                .build();

        tokenRepository.save(token);

        emailService.send(
                user.getEmail(),
                "Okane Transfer — Réinitialisation de mot de passe",
                "Votre code de réinitialisation est : " + code + "\nCe code expire dans 15 minutes."
        );
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO dto) {
        Token token = tokenRepository.findByValeurAndType(dto.getToken(), TypeToken.PASSWORD_RESET)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (token.getDateExpiration().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            throw new BadRequestException("Reset token has expired");
        }

        User user = token.getUtilisateur();
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        tokenRepository.delete(token);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword()))
            throw new BadCredentialsException("Current password is incorrect");

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    private AuthResponseDTO buildTokens(User user) {
        return AuthResponseDTO.builder()
                .accessToken(jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name()))
                .refreshToken(jwtUtil.generateRefreshToken(user.getEmail()))
                .build();
    }
}