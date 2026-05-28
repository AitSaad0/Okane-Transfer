package com.okane.service.impl;

import com.okane.entity.Transfert;
import com.okane.dto.ComplianceDashboardDto;
import com.okane.dto.SarDto;
import com.okane.dto.requestDto.ThresholdRequest;
import com.okane.entity.ComplianceThreshold;
import com.okane.entity.SarReport;
import com.okane.repository.ComplianceThresholdRepository;
import com.okane.repository.SarReportRepository;
import com.okane.service.ComplianceService;
import com.okane.repository.TransfertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplianceServiceImpl implements ComplianceService {

    private final SarReportRepository sarReportRepository;
    private final ComplianceThresholdRepository thresholdRepository;
    private final TransfertRepository transfertRepository;

    @Override
    public void analyzeTransfer(Transfert transfert) {

        ComplianceThreshold threshold = thresholdRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    ComplianceThreshold t = new ComplianceThreshold();
                    t.setSarThreshold(new BigDecimal("10000"));
                    t.setUpdatedAt(LocalDateTime.now());
                    return thresholdRepository.save(t);
                });

        if (transfert.getMontantEnvoye()
                .compareTo(threshold.getSarThreshold()) >= 0) {

            transfert.setEstSuspect(true);

            SarReport sar = SarReport.builder()
                    .referenceCode("SAR-" + System.currentTimeMillis())
                    .reason("Transfer exceeds AML threshold")
                    .thresholdAmount(threshold.getSarThreshold())
                    .transferAmount(transfert.getMontantEnvoye())
                    .status("OPEN")
                    .createdAt(LocalDateTime.now())
                    .transfert(transfert)
                    .build();

            sarReportRepository.save(sar);

            transfertRepository.save(transfert);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SarDto> getAllSarReports() {

        return sarReportRepository.findAllWithTransfert()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SarDto getSarById(Long id) {

        return sarReportRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() ->
                        new RuntimeException("SAR not found"));
    }

    @Override
    public ComplianceDashboardDto getDashboard() {

        long totalTransfers = transfertRepository.count();

        long suspiciousTransfers =
                transfertRepository.countByEstSuspectTrue();

        long totalSar = sarReportRepository.count();

        long openSar =
                sarReportRepository.countByStatus("OPEN");

        double rate = totalTransfers == 0
                ? 0
                : ((double) suspiciousTransfers / totalTransfers) * 100;

        return ComplianceDashboardDto.builder()
                .totalTransfers(totalTransfers)
                .suspiciousTransfers(suspiciousTransfers)
                .totalSarReports(totalSar)
                .openSarReports(openSar)
                .suspiciousRate(rate)
                .build();
    }

    @Override
    public void updateThreshold(ThresholdRequest request) {

        ComplianceThreshold threshold = thresholdRepository
                .findAll()
                .stream()
                .findFirst()
                .orElse(new ComplianceThreshold());

        threshold.setSarThreshold(request.getSarThreshold());
        threshold.setUpdatedAt(LocalDateTime.now());

        thresholdRepository.save(threshold);
    }

    private SarDto mapToDto(SarReport sar) {

        return SarDto.builder()
                .id(sar.getId())
                .referenceCode(sar.getReferenceCode())
                .reason(sar.getReason())
                .thresholdAmount(sar.getThresholdAmount())
                .transferAmount(sar.getTransferAmount())
                .status(sar.getStatus())
                .transferCode(sar.getTransfert().getCodeRetrait())
                .createdAt(sar.getCreatedAt())
                .build();
    }
}