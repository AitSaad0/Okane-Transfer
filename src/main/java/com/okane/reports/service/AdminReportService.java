package com.okane.reports.service;

import com.okane.reports.dto.*;

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