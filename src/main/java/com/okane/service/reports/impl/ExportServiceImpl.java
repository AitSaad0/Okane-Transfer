package com.okane.service.reports.impl;

import com.okane.dto.reports.DailyReportDto;
import com.okane.service.reports.ExportService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@Service
public class ExportServiceImpl implements ExportService {

    @Override
    public ByteArrayInputStream exportDailyCsv(
            List<DailyReportDto> reports
    ) {

        StringBuilder sb = new StringBuilder();

        sb.append("Corridor,Transactions\n");

        for (DailyReportDto r : reports) {

            sb.append(r.getCorridor())
                    .append(",")
                    .append(r.getTransactionCount())
                    .append("\n");
        }

        return new ByteArrayInputStream(
                sb.toString().getBytes()
        );
    }

    @Override
    public ByteArrayInputStream exportDailyPdf(
            List<DailyReportDto> reports
    ) {

        return new ByteArrayInputStream(
                "PDF NOT IMPLEMENTED".getBytes()
        );
    }
}
