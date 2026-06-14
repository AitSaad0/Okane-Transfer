package com.okane.service;

import com.okane.entity.Transfert;
import com.okane.dto.ComplianceDashboardDto;
import com.okane.dto.SarDto;
import com.okane.dto.requestDto.ThresholdRequest;

import java.util.List;

public interface ComplianceService {

    void analyzeTransfer(Transfert transfert);

    List<SarDto> getAllSarReports();

    SarDto getSarById(Long id);

    ComplianceDashboardDto getDashboard();

    void updateThreshold(ThresholdRequest request);
}