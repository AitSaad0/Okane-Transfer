package com.okane.controller;

import com.okane.dto.requestDto.CorridorRequestDTO;
import com.okane.dto.responseDto.CorridorResponseDTO;
import com.okane.service.CorridorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/corridors")
public class CorridorController {

    private final CorridorService corridorService;

    public CorridorController(CorridorService corridorService) {
        this.corridorService = corridorService;
    }

    @GetMapping
    public ResponseEntity<List<CorridorResponseDTO>> getAll() {
        return ResponseEntity.ok(corridorService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CorridorResponseDTO>> getActive() {
        return ResponseEntity.ok(corridorService.findActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorridorResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(corridorService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CorridorResponseDTO> create(@Valid @RequestBody CorridorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(corridorService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CorridorResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CorridorRequestDTO dto) {
        return ResponseEntity.ok(corridorService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        corridorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CorridorResponseDTO> toggleStatus(@PathVariable Long id) {
        corridorService.toggleStatus(id);
        return ResponseEntity.ok(corridorService.findById(id));
    }
}