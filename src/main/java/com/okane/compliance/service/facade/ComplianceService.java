package com.okane.compliance.service.facade;

import com.okane.clients_transfers.bean.Transfert;
import com.okane.compliance.controller.dto.*;

import java.util.List;

public interface ComplianceService {

    void analyzeTransfer(Transfert transfert);

    List<SarDto> getAllSarReports();

    SarDto getSarById(Long id);

    ComplianceDashboardDto getDashboard();

    void updateThreshold(ThresholdRequest request);
}