package com.okane.controller;

import com.okane.service.ComplianceService;

import com.okane.dto.ComplianceDashboardDto;
import com.okane.dto.SarDto;
import com.okane.dto.requestDto.ThresholdRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/compliance")
@RequiredArgsConstructor
public class ComplianceController {

    private final ComplianceService complianceService;

    @GetMapping("/sar")
    public ResponseEntity<List<SarDto>> getAllSar() {

        return ResponseEntity.ok(
                complianceService.getAllSarReports()
        );
    }

    @GetMapping("/sar/{id}")
    public ResponseEntity<SarDto> getSar(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                complianceService.getSarById(id)
        );
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ComplianceDashboardDto> dashboard() {

        return ResponseEntity.ok(
                complianceService.getDashboard()
        );
    }

    @PutMapping("/thresholds")
    public ResponseEntity<String> updateThreshold(
            @Valid @RequestBody ThresholdRequest request
    ) {

        complianceService.updateThreshold(request);

        return ResponseEntity.ok(
                "AML threshold updated successfully"
        );
    }
}