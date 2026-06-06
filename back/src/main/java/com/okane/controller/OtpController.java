package com.okane.controller;

import com.okane.dto.requestDto.OtpVerifyRequestDTO;
import com.okane.dto.responseDto.OtpResponseDTO;
import com.okane.entity.User;
import com.okane.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/2fa")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    public ResponseEntity<OtpResponseDTO> send(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        otpService.send(userId);
        return ResponseEntity.ok(OtpResponseDTO.builder()
                .message("OTP sent successfully")
                .success(true)
                .build());
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpResponseDTO> verify(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody OtpVerifyRequestDTO request) {
        Long userId = ((User) userDetails).getId();
        otpService.verify(userId, request.getValeur());
        return ResponseEntity.ok(OtpResponseDTO.builder()
                .message("OTP verified successfully")
                .success(true)
                .build());
    }
}