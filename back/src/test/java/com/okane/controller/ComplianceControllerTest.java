package com.okane.controller;

import com.okane.dto.ComplianceDashboardDto;
import com.okane.dto.SarDto;
import com.okane.dto.requestDto.ThresholdRequest;
import com.okane.service.ComplianceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ComplianceControllerTest {
    private MockMvc mockMvc;
    @Mock
    private ComplianceService complianceService;
    @InjectMocks
    private ComplianceController complianceController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(complianceController).build();
    }

    @Test
    void getAllSar_ShouldReturnList() throws Exception {
        List<SarDto> sarList = Arrays.asList(new SarDto());
        when(complianceService.getAllSarReports()).thenReturn(sarList);
        mockMvc.perform(get("/api/v1/admin/compliance/sar"))
                .andExpect(status().isOk());
        verify(complianceService).getAllSarReports();
    }

    @Test
    void getSar_ShouldReturnSar() throws Exception {
        SarDto sarDto = new SarDto();
        when(complianceService.getSarById(anyLong())).thenReturn(sarDto);
        mockMvc.perform(get("/api/v1/admin/compliance/sar/{id}", 1L))
                .andExpect(status().isOk());
        verify(complianceService).getSarById(anyLong());
    }

    @Test
    void dashboard_ShouldReturnDashboard() throws Exception {
        ComplianceDashboardDto dashboardDto = new ComplianceDashboardDto();
        when(complianceService.getDashboard()).thenReturn(dashboardDto);
        mockMvc.perform(get("/api/v1/admin/compliance/dashboard"))
                .andExpect(status().isOk());
        verify(complianceService).getDashboard();
    }

    @Test
    void updateThreshold_ShouldReturnOk() throws Exception {
        ThresholdRequest request = new ThresholdRequest();
        doNothing().when(complianceService).updateThreshold(any(ThresholdRequest.class));
        mockMvc.perform(put("/api/v1/admin/compliance/thresholds")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
        verify(complianceService).updateThreshold(any(ThresholdRequest.class));
    }
}
