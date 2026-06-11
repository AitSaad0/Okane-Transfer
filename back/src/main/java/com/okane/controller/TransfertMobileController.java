package com.okane.controller;

import com.okane.dto.requestDto.TransfertMobileRequestDTO;
import com.okane.dto.responseDto.TransfertMobileResponseDTO;
import com.okane.service.TransfertMobileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent/transfers")
public class TransfertMobileController {

    @Autowired
    private TransfertMobileService transfertMobileService;

    @PostMapping("/mobile")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<TransfertMobileResponseDTO> creerTransfertMobile(
            @Valid @RequestBody TransfertMobileRequestDTO request,
            Authentication authentication) {
        TransfertMobileResponseDTO response = transfertMobileService.creerTransfertMobile(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
