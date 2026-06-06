package com.okane.controller;

import com.okane.dto.requestDto.TauxChangeRequestDTO;
import com.okane.dto.responseDto.ConversionResponseDTO;
import com.okane.dto.responseDto.TauxChangeResponseDTO;
import com.okane.service.TauxChangeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TauxChangeController {

    @Autowired
    private TauxChangeService tauxChangeService;

    @GetMapping("/exchange-rates")
    public ResponseEntity<List<TauxChangeResponseDTO>> getCurrentRates() {
        return ResponseEntity.ok(tauxChangeService.findAllCurrentRates());
    }

    @GetMapping("/exchange-rates/convert")
    public ResponseEntity<ConversionResponseDTO> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(tauxChangeService.convert(from, to, amount));
    }

    @PutMapping("/admin/exchange-rates/{corridorId}")

    public ResponseEntity<TauxChangeResponseDTO> updateManual(
            @PathVariable Long corridorId,
            @Valid @RequestBody TauxChangeRequestDTO dto) {
        return ResponseEntity.ok(tauxChangeService.updateManual(corridorId, dto));
    }

    @PostMapping("/admin/exchange-rates/sync")

    public ResponseEntity<String> syncFromExternalApi() {
        tauxChangeService.syncFromExternalApi();
        return ResponseEntity.ok("Synchronisation terminée");
    }

    @GetMapping("/admin/exchange-rates/history/{corridorId}")

    public ResponseEntity<List<TauxChangeResponseDTO>> getHistory(
            @PathVariable Long corridorId) {
        return ResponseEntity.ok(tauxChangeService.getHistory(corridorId));
    }
}