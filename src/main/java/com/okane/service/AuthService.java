package com.okane.service;

import com.okane.dto.requestDto.AuthRequestDTO;
import com.okane.dto.requestDto.RegisterRequestDTO;
import com.okane.dto.responseDto.AuthResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    public AuthResponseDTO register(RegisterRequestDTO dto);
    public AuthResponseDTO login(AuthRequestDTO dto);
}
