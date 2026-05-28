package com.okane.controller;

import com.okane.dto.requestDto.PaysRequestDTO;
import com.okane.dto.responseDto.PaysResponseDTO;
import com.okane.service.PaysService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/countries")
public class PaysController {

    private final PaysService paysService;

    public PaysController(PaysService paysService) {
        this.paysService = paysService;
    }

    @GetMapping
    public ResponseEntity<List<PaysResponseDTO>> getAll() {
        return ResponseEntity.ok(paysService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaysResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paysService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PaysResponseDTO> create(@RequestBody PaysRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paysService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaysResponseDTO> update(@PathVariable Long id, @RequestBody PaysRequestDTO dto) {
        return ResponseEntity.ok(paysService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paysService.delete(id);
        return ResponseEntity.noContent().build();
    }
}