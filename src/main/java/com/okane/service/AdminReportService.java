package com.okane.service;

import com.okane.dto.AlertDto;
import com.okane.dto.CorridorPerformanceDto;
import com.okane.dto.DailyReportDto;
import com.okane.dto.MonthlyReportDto;

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