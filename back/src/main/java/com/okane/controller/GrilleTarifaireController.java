package com.okane.controller;

import com.okane.dto.requestDto.GrilleTarifaireRequestDTO;
import com.okane.dto.requestDto.SimulationRequestDTO;
import com.okane.dto.responseDto.GrilleTarifaireResponseDTO;
import com.okane.dto.responseDto.SimulationResponseDTO;
import com.okane.service.GrilleTarifaireService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GrilleTarifaireController {

    @Autowired
    private GrilleTarifaireService grilleTarifaireService;


    @GetMapping("/api/v1/admin/fee-grids")

    public ResponseEntity<List<GrilleTarifaireResponseDTO>> getAll() {
        return ResponseEntity.ok(grilleTarifaireService.findAll());
    }


    @GetMapping("/api/v1/admin/fee-grids/{id}")

    public ResponseEntity<GrilleTarifaireResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(grilleTarifaireService.findById(id));
    }


    @GetMapping("/api/v1/admin/fee-grids/corridor/{corridorId}")

    public ResponseEntity<List<GrilleTarifaireResponseDTO>> getByCorridor(@PathVariable Long corridorId) {
        return ResponseEntity.ok(grilleTarifaireService.findByCorridorId(corridorId));
    }


    @PostMapping("/api/v1/admin/fee-grids")

    public ResponseEntity<GrilleTarifaireResponseDTO> create(@Valid @RequestBody GrilleTarifaireRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grilleTarifaireService.save(dto));
    }


    @PutMapping("/api/v1/admin/fee-grids/{id}")

    public ResponseEntity<GrilleTarifaireResponseDTO> update(@PathVariable Long id, @Valid @RequestBody GrilleTarifaireRequestDTO dto) {
        return ResponseEntity.ok(grilleTarifaireService.update(id, dto));
    }


    @DeleteMapping("/api/v1/admin/fee-grids/{id}")

    public ResponseEntity<Void> delete(@PathVariable Long id) {
        grilleTarifaireService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/api/v1/admin/fee-grids/export")

    public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "csv") String format) {
        byte[] data;
        String filename;
        MediaType mediaType;

        if ("pdf".equalsIgnoreCase(format)) {
            data = grilleTarifaireService.exportPdf();
            filename = "grilles_tarifaires.pdf";
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            data = grilleTarifaireService.exportCsv();
            filename = "grilles_tarifaires.csv";
            mediaType = MediaType.TEXT_PLAIN;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(data);
    }


    @PostMapping("/api/v1/fees/simulate")

    public ResponseEntity<SimulationResponseDTO> simulate(@Valid @RequestBody SimulationRequestDTO dto) {
        return ResponseEntity.ok(grilleTarifaireService.simulate(dto));
    }
}