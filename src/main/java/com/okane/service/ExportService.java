package com.okane.service;

import com.okane.dto.DailyReportDto;

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