package com.okane.controller.admin;

import com.okane.entity.Devise;
import com.okane.service.DeviseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<Devise>> getAll() {
        return ResponseEntity.ok(deviseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Devise> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(deviseService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Devise> create(@RequestBody Devise devise) {
        Devise saved = deviseService.save(devise);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Devise> update(@PathVariable UUID id, @RequestBody Devise devise) {
        return ResponseEntity.ok(deviseService.update(id, devise));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deviseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}