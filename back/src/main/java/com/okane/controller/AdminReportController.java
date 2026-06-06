package com.okane.controller;

import com.okane.dto.AlertDto;
import com.okane.dto.CorridorPerformanceDto;
import com.okane.dto.DailyReportDto;
import com.okane.dto.MonthlyReportDto;
import com.okane.service.AdminReportService;
import com.okane.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;
    private final ExportService exportService;

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
    public ResponseEntity<byte[]> exportReport(

            @RequestParam String format,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(required = false)
            UUID corridorId

    ) throws IOException {

        List<DailyReportDto> reports =
                adminReportService.getDailyReport(
                        date,
                        corridorId,
                        null
                );

        ByteArrayInputStream file;

        String filename;

        String contentType;

        if (format.equalsIgnoreCase("csv")) {

            file = exportService.exportDailyCsv(reports);

            filename = "daily-report.csv";

            contentType = "text/csv";

        } else if (format.equalsIgnoreCase("pdf")) {

            file = exportService.exportDailyPdf(reports);

            filename = "daily-report.pdf";

            contentType = "application/pdf";

        } else {

            throw new RuntimeException(
                    "Unsupported format: " + format
            );
        }

        byte[] bytes = file.readAllBytes();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename
                )
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }
}