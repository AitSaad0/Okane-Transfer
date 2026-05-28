package com.okane.reports.service.facade;

import com.okane.reports.controller.dto.AlertDto;
import com.okane.reports.controller.dto.CorridorPerformanceDto;
import com.okane.reports.controller.dto.DailyReportDto;
import com.okane.reports.controller.dto.MonthlyReportDto;

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