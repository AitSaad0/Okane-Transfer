package com.okane.controller;

import com.okane.dto.requestDto.TransfertRequestDTO;
import com.okane.dto.responseDto.TransfertResponseDTO;
import com.okane.service.TransfertService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent/transfers")
public class TransfertController {

    @Autowired
    private TransfertService transfertService;

    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<TransfertResponseDTO> creerTransfert(
            @Valid @RequestBody TransfertRequestDTO request,
            Authentication authentication) {
        TransfertResponseDTO response = transfertService.creerTransfert(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
