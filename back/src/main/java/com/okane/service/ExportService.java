package com.okane.service;

import com.okane.dto.CorridorPerformanceDto;
import com.okane.dto.DailyReportDto;
import com.okane.dto.MonthlyReportDto;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface ExportService {

    ByteArrayInputStream exportDailyCsv(
            List<DailyReportDto> reports
    );

    ByteArrayInputStream exportDailyPdf(
            List<DailyReportDto> reports
    );
    ByteArrayInputStream exportMonthlyCsv(List<MonthlyReportDto> reports);
    ByteArrayInputStream exportMonthlyPdf(List<MonthlyReportDto> reports);
    ByteArrayInputStream exportCorridorCsv(List<CorridorPerformanceDto> reports);
    ByteArrayInputStream exportCorridorPdf(List<CorridorPerformanceDto> reports);
}