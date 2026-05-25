package com.okane.service.reports;

import com.okane.dto.reports.DailyReportDto;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface ExportService {

    ByteArrayInputStream exportDailyCsv(
            List<DailyReportDto> reports
    );

    ByteArrayInputStream exportDailyPdf(
            List<DailyReportDto> reports
    );
}