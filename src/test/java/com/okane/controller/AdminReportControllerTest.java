package com.okane.controller;

import com.okane.dto.AlertDto;
import com.okane.dto.CorridorPerformanceDto;
import com.okane.dto.DailyReportDto;
import com.okane.dto.MonthlyReportDto;
import com.okane.service.AdminReportService;
import com.okane.service.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminReportControllerTest {
    private MockMvc mockMvc;
    @Mock
    private AdminReportService adminReportService;
    @Mock
    private ExportService exportService;
    @InjectMocks
    private AdminReportController adminReportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminReportController).build();
    }

    @Test
    void getDailyReport_ShouldReturnList() throws Exception {
        List<DailyReportDto> reports = Arrays.asList(new DailyReportDto(), new DailyReportDto());
        when(adminReportService.getDailyReport(any(), any(), any())).thenReturn(reports);
        mockMvc.perform(get("/api/v1/admin/reports/daily")
                .param("date", "2024-01-01"))
                .andExpect(status().isOk());
        verify(adminReportService).getDailyReport(any(), any(), any());
    }

    @Test
    void getMonthlyReport_ShouldReturnList() throws Exception {
        List<MonthlyReportDto> reports = Arrays.asList(new MonthlyReportDto());
        when(adminReportService.getMonthlyReport(anyInt())).thenReturn(reports);
        mockMvc.perform(get("/api/v1/admin/reports/monthly").param("year", "2024"))
                .andExpect(status().isOk());
        verify(adminReportService).getMonthlyReport(anyInt());
    }

    @Test
    void getCorridorPerformance_ShouldReturnList() throws Exception {
        List<CorridorPerformanceDto> list = Arrays.asList(new CorridorPerformanceDto());
        when(adminReportService.getCorridorPerformance(any(), any())).thenReturn(list);
        mockMvc.perform(get("/api/v1/admin/reports/corridors")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isOk());
        verify(adminReportService).getCorridorPerformance(any(), any());
    }

    @Test
    void getAlerts_ShouldReturnList() throws Exception {
        List<AlertDto> alerts = Arrays.asList(new AlertDto());
        when(adminReportService.getActiveAlerts()).thenReturn(alerts);
        mockMvc.perform(get("/api/v1/admin/reports/alerts"))
                .andExpect(status().isOk());
        verify(adminReportService).getActiveAlerts();
    }

    @Test
    void exportReport_ShouldReturnCsv() throws Exception {
        List<DailyReportDto> reports = Arrays.asList(new DailyReportDto());
        when(adminReportService.getDailyReport(any(), any(), isNull())).thenReturn(reports);
        when(exportService.exportDailyCsv(anyList())).thenReturn(new ByteArrayInputStream("csv-data".getBytes()));
        mockMvc.perform(get("/api/v1/admin/reports/export")
                .param("format", "csv")
                .param("date", "2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("daily-report.csv")))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    void exportReport_ShouldReturnPdf() throws Exception {
        List<DailyReportDto> reports = Arrays.asList(new DailyReportDto());
        when(adminReportService.getDailyReport(any(), any(), isNull())).thenReturn(reports);
        when(exportService.exportDailyPdf(anyList())).thenReturn(new ByteArrayInputStream("pdf-data".getBytes()));
        mockMvc.perform(get("/api/v1/admin/reports/export")
                .param("format", "pdf")
                .param("date", "2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("daily-report.pdf")))
                .andExpect(content().contentType("application/pdf"));
    }
}
