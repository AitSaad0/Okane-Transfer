package com.okane.service.reports;

import com.okane.dto.reports.AlertDto;
import com.okane.dto.reports.CorridorPerformanceDto;
import com.okane.dto.reports.DailyReportDto;
import com.okane.dto.reports.MonthlyReportDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AdminReportService {

    List<DailyReportDto> getDailyReport(
            LocalDate date,
            UUID corridorId,
            UUID agenceId
    );

    List<MonthlyReportDto> getMonthlyReport(
            int year
    );

    List<CorridorPerformanceDto> getCorridorPerformance(
            LocalDate startDate,
            LocalDate endDate
    );

    List<AlertDto> getActiveAlerts();
}