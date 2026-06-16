package com.okane.service.impl;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4.rotate());

            PdfWriter.getInstance(document, out);

            document.open();

            Paragraph title =
                    new Paragraph("OKANE - DAILY REPORT", titleFont());

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);

            document.add(
                    new Paragraph(
                            "Generated: " + LocalDateTime.now(),
                            normalFont()
                    )
            );

            document.add(Chunk.NEWLINE);

            long totalTransactions = 0;
            BigDecimal totalVolume = BigDecimal.ZERO;
            BigDecimal totalRevenue = BigDecimal.ZERO;

            for (DailyReportDto r : reports) {

                totalTransactions += r.getTransactionCount();

                totalVolume =
                        totalVolume.add(r.getTotalVolume());

                totalRevenue =
                        totalRevenue.add(r.getTotalRevenue());
            }

            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(40);

            summary.addCell("Total Transactions");
            summary.addCell(String.valueOf(totalTransactions));

            summary.addCell("Total Volume");
            summary.addCell(money(totalVolume));

            summary.addCell("Total Revenue");
            summary.addCell(money(totalRevenue));

            document.add(summary);

            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            table.setWidths(
                    new float[]{3,1,2,2,2,2}
            );

            table.addCell(headerCell("Corridor"));
            table.addCell(headerCell("Transactions"));
            table.addCell(headerCell("Volume"));
            table.addCell(headerCell("Revenue"));
            table.addCell(headerCell("Agency Commission"));
            table.addCell(headerCell("Central Commission"));

            for (DailyReportDto r : reports) {

                table.addCell(r.getCorridor());
                table.addCell(String.valueOf(r.getTransactionCount()));
                table.addCell(money(r.getTotalVolume()));
                table.addCell(money(r.getTotalRevenue()));
                table.addCell(money(r.getAgencyCommission()));
                table.addCell(money(r.getCentralCommission()));
            }

            document.add(table);

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
    public ByteArrayInputStream exportMonthlyPdf(
            List<MonthlyReportDto> reports
    ) {

        try {

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(PageSize.A4.rotate());

            PdfWriter.getInstance(document, out);

            document.open();

            Paragraph title =
                    new Paragraph(
                            "OKANE - MONTHLY REPORT",
                            titleFont()
                    );

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);

            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);

            table.setWidthPercentage(100);

            table.addCell(headerCell("Month"));
            table.addCell(headerCell("Corridor"));
            table.addCell(headerCell("Transactions"));
            table.addCell(headerCell("Volume"));
            table.addCell(headerCell("Revenue"));

            for (MonthlyReportDto r : reports) {

                table.addCell(r.getMonth());
                table.addCell(r.getCorridor());
                table.addCell(String.valueOf(r.getTransactionCount()));
                table.addCell(money(r.getTotalVolume()));
                table.addCell(money(r.getTotalRevenue()));
            }

            document.add(table);

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
    public ByteArrayInputStream exportCorridorPdf(
            List<CorridorPerformanceDto> reports
    ) {

        try {

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(PageSize.A4.rotate());

            PdfWriter.getInstance(document, out);

            document.open();

            Paragraph title =
                    new Paragraph(
                            "OKANE - CORRIDOR PERFORMANCE",
                            titleFont()
                    );

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);

            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);

            table.setWidthPercentage(100);

            table.addCell(headerCell("Corridor"));
            table.addCell(headerCell("Transactions"));
            table.addCell(headerCell("Volume"));
            table.addCell(headerCell("Average Transfer"));
            table.addCell(headerCell("Generated Fees"));

            for (CorridorPerformanceDto r : reports) {

                table.addCell(r.getCorridor());
                table.addCell(String.valueOf(r.getTotalTransactions()));
                table.addCell(money(r.getTotalVolume()));
                table.addCell(money(r.getAverageTransfer()));
                table.addCell(money(r.getGeneratedFees()));
            }

            document.add(table);

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
    private Font titleFont() {
        return new Font(Font.HELVETICA, 18, Font.BOLD);
    }

    private Font sectionFont() {
        return new Font(Font.HELVETICA, 12, Font.BOLD);
    }

    private Font normalFont() {
        return new Font(Font.HELVETICA, 10);
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, sectionFont()));
        cell.setBackgroundColor(new Color(41, 128, 185));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        return cell;
    }

    private String money(BigDecimal value) {
        return value == null ? "0" : String.format("%,.2f", value);
    }
}
