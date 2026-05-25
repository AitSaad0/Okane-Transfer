package com.okane.reports.controller;

import com.okane.reports.dto.*;
import com.okane.reports.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/daily")
    public ResponseEntity<List<DailyReportDto>> getDailyReport(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(required = false)
            UUID corridorId,

            @RequestParam(required = false)
            UUID agenceId
    ) {

        return ResponseEntity.ok(
                adminReportService.getDailyReport(
                        date,
                        corridorId,
                        agenceId
                )
        );
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyReportDto>> getMonthlyReport(
            @RequestParam int year
    ) {

        return ResponseEntity.ok(
                adminReportService.getMonthlyReport(year)
        );
    }

    @GetMapping("/corridors")
    public ResponseEntity<List<CorridorPerformanceDto>>
    getCorridorPerformance(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {

        return ResponseEntity.ok(
                adminReportService.getCorridorPerformance(
                        startDate,
                        endDate
                )
        );
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertDto>> getAlerts() {

        return ResponseEntity.ok(
                adminReportService.getActiveAlerts()
        );
    }
    @GetMapping("/export")
    public ResponseEntity<String> exportReport(
            @RequestParam String format,
            @RequestParam String period
    ) {

        return ResponseEntity.ok(
                "Export generated in format: "
                        + format
                        + " for period: "
                        + period
        );
    }
}