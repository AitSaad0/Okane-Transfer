package com.okane.service.impl;

import com.okane.dto.CorridorPerformanceDto;
import com.okane.dto.DailyReportDto;
import com.okane.dto.MonthlyReportDto;
import com.okane.service.ExportService;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

        try {

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, out);

            document.open();

            document.add(new Paragraph("Daily Report"));
            document.add(new Paragraph(" "));

            for (DailyReportDto r : reports) {

                document.add(
                        new Paragraph(
                                r.getCorridor()
                                        + " : "
                                        + r.getTransactionCount()
                        )
                );
            }

            document.close();

            return new ByteArrayInputStream(
                    out.toByteArray()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate PDF",
                    e
            );
        }
    }
    @Override
    public ByteArrayInputStream exportMonthlyCsv(List<MonthlyReportDto> reports) {
        StringBuilder sb = new StringBuilder();
        sb.append("Month,Corridor,TransactionCount,TotalVolume,TotalRevenue\n");
        for (MonthlyReportDto r : reports) {
            sb.append(r.getMonth()).append(",")
                    .append(r.getCorridor()).append(",")
                    .append(r.getTransactionCount()).append(",")
                    .append(r.getTotalVolume()).append(",")
                    .append(r.getTotalRevenue()).append("\n");
        }
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    @Override
    public ByteArrayInputStream exportMonthlyPdf(List<MonthlyReportDto> reports) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Monthly Report"));
            document.add(new Paragraph(" "));
            for (MonthlyReportDto r : reports) {
                document.add(new Paragraph(
                        r.getMonth() + " | " +
                                r.getCorridor() + " | " +
                                "Transactions: " + r.getTransactionCount() + " | " +
                                "Volume: " + r.getTotalVolume() + " | " +
                                "Revenue: " + r.getTotalRevenue()
                ));
            }
            document.close();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    @Override
    public ByteArrayInputStream exportCorridorCsv(List<CorridorPerformanceDto> reports) {
        StringBuilder sb = new StringBuilder();
        sb.append("Corridor,TotalTransactions,TotalVolume\n");
        for (CorridorPerformanceDto r : reports) {
            sb.append(r.getCorridor()).append(",")
                    .append(r.getTotalTransactions()).append(",")
                    .append(r.getTotalVolume()).append("\n");
        }
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    @Override
    public ByteArrayInputStream exportCorridorPdf(List<CorridorPerformanceDto> reports) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Corridor Performance Report"));
            document.add(new Paragraph(" "));
            for (CorridorPerformanceDto r : reports) {
                document.add(new Paragraph(r.getCorridor() + " : " + r.getTotalTransactions()));
            }
            document.close();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
