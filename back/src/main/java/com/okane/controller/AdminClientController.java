package com.okane.controller;

import com.okane.dto.responseDto.ClientSearchResponseDto;
import com.okane.entity.Client;
import com.okane.repository.ClientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clients")
@PreAuthorize("hasRole('ADMIN')")
public class AdminClientController {

    private final ClientRepository clientRepository;

    public AdminClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @GetMapping
    public ResponseEntity<List<ClientSearchResponseDto>> searchClients(
            @RequestParam(required = false, defaultValue = "") String search
    ) {

        List<Client> clients = search.isBlank()
                ? clientRepository.findAll()
                : clientRepository.searchClients(search);

        List<ClientSearchResponseDto> result = clients.stream()
                .map(c -> new ClientSearchResponseDto(
                        c.getId(),
                        c.getNom(),
                        c.getPrenom(),
                        c.getNumPieceIdentite(),
                        c.getTelephone(),
                        c.getEmail(),
                        c.getEstSurListeSurveillance()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }
}