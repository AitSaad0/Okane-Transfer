package com.okane.geographic_monetary_reference.controller.facade;

import com.okane.geographic_monetary_reference.bean.Devise;
import com.okane.geographic_monetary_reference.controller.dto.requestDto.DeviseRequestDTO;
import com.okane.geographic_monetary_reference.controller.dto.responseDto.DeviseResponseDTO;
import com.okane.geographic_monetary_reference.service.impl.DeviseServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.okane.geographic_monetary_reference.service.facade.DeviseService;

import java.util.List;
import java.util.UUID;

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