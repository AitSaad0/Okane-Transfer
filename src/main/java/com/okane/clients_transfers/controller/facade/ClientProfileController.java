package com.okane.clients_transfers.controller.facade;

import com.okane.clients_transfers.controller.dto.requestDto.UpdateClientProfileRequestDto;
import com.okane.clients_transfers.controller.dto.responseDto.ClientActivityResponseDto;
import com.okane.clients_transfers.controller.dto.responseDto.ClientProfileResponseDto;
import com.okane.pagination.PageResponseDto;
import com.okane.clients_transfers.service.facade.ClientProfileService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clients/profile")
@RequiredArgsConstructor
//@Tag(name = "Client — Profil", description = "Consultation et mise à jour du profil client")
public class ClientProfileController {

    private final ClientProfileService clientProfileService;

    @GetMapping
    //@Operation(summary = "Retourne le profil complet du client connecté")
    public ResponseEntity<ClientProfileResponseDto> getMyProfile(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(clientProfileService.getMyProfile(userId));
    }

    @PutMapping
    //@Operation(summary = "Met à jour nom, téléphone, préférences de notification")
    public ResponseEntity<ClientProfileResponseDto> updateMyProfile(
            @RequestParam Long userId,
            @Valid @RequestBody UpdateClientProfileRequestDto request
    ) {
        return ResponseEntity.ok(clientProfileService.updateMyProfile(userId, request));
    }

    @GetMapping("/activity")
    //@Operation(summary = "Historique des connexions et actions du compte")
    public ResponseEntity<PageResponseDto<ClientActivityResponseDto>> getMyActivity(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(clientProfileService.getMyActivity(userId, page, size));
    }
}