package com.okane.controller;

import com.okane.dto.requestDto.CorridorRequestDTO;
import com.okane.dto.requestDto.CorridorByCodeRequestDTO;
import com.okane.dto.responseDto.CorridorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.okane.service.CorridorService;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<CorridorResponseDTO> create(@RequestBody CorridorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(corridorService.save(dto));
    }

    @PostMapping("/by-code")
    public ResponseEntity<CorridorResponseDTO> createByCode(@RequestBody CorridorByCodeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(corridorService.saveByCode(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CorridorResponseDTO> update(@PathVariable Long id, @RequestBody CorridorRequestDTO dto) {
        return ResponseEntity.ok(corridorService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        corridorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CorridorResponseDTO> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(corridorService.toggleStatus(id, body.get("active")));
    }
}