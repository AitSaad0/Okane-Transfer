package com.okane.controller;

import com.okane.dto.requestDto.DeviseRequestDTO;
import com.okane.dto.responseDto.DeviseResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.okane.service.DeviseService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/currencies")
public class DeviseController {

    private final DeviseService deviseService;

    public DeviseController(DeviseService deviseService) {

        this.deviseService = deviseService;
    }

    @GetMapping
    public ResponseEntity<List<DeviseResponseDTO>> getAll() {

        return ResponseEntity.ok(deviseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviseResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deviseService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DeviseResponseDTO> create(@RequestBody DeviseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviseService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviseResponseDTO> update(@PathVariable Long id, @RequestBody DeviseRequestDTO dto) {
        return ResponseEntity.ok(deviseService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deviseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}