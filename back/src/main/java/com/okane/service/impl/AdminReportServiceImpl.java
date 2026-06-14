package com.okane.service.impl;

import com.okane.dto.AlertDto;
import com.okane.dto.CorridorPerformanceDto;
import com.okane.dto.DailyReportDto;
import com.okane.dto.MonthlyReportDto;
import com.okane.entity.Transfert;
import com.okane.repository.KycAlertRepository;
import com.okane.repository.TransfertRepository;
import com.okane.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final TransfertRepository transfertRepository;
    private final KycAlertRepository kycAlertRepository;
    @Override
    public List<DailyReportDto> getDailyReport(
            LocalDate date,
            UUID corridorId,
            UUID agenceId
    ) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Transfert> transfers =
                transfertRepository.findByPeriod(start, end);

        Map<String, DailyReportDto> map = new HashMap<>();

        for (Transfert t : transfers) {

            String corridorName =
                    t.getCorridor().getPaysOrigine().getNom()
                            + " -> "
                            + t.getCorridor().getPaysDestination().getNom();

            DailyReportDto dto =
                    map.getOrDefault(
                            corridorName,
                            DailyReportDto.builder()
                                    .corridor(corridorName)
                                    .transactionCount(0L)
                                    .totalVolume(BigDecimal.ZERO)
                                    .totalRevenue(BigDecimal.ZERO)
                                    .agencyCommission(BigDecimal.ZERO)
                                    .centralCommission(BigDecimal.ZERO)
                                    .build()
                    );

            dto.setTransactionCount(dto.getTransactionCount() + 1);

            dto.setTotalVolume(
                    dto.getTotalVolume()
                            .add(t.getMontantEnvoye())
            );

            dto.setTotalRevenue(
                    dto.getTotalRevenue()
                            .add(t.getFrais())
            );

            BigDecimal agencyPart =
                    t.getFrais().multiply(BigDecimal.valueOf(0.7));

            BigDecimal centralPart =
                    t.getFrais().multiply(BigDecimal.valueOf(0.3));

            dto.setAgencyCommission(
                    dto.getAgencyCommission().add(agencyPart)
            );

            dto.setCentralCommission(
                    dto.getCentralCommission().add(centralPart)
            );

            map.put(corridorName, dto);
        }

        return new ArrayList<>(map.values());
    }

    @Override
    public List<MonthlyReportDto> getMonthlyReport(int year) {

        LocalDateTime start =
                LocalDate.of(year, 1, 1).atStartOfDay();

        LocalDateTime end =
                LocalDate.of(year + 1, 1, 1).atStartOfDay();

        List<Transfert> transfers =
                transfertRepository.findByPeriod(start, end);

        Map<String, MonthlyReportDto> map = new HashMap<>();

        for (Transfert t : transfers) {

            String month =
                    t.getDateCreation().getMonth().name();

            String corridor =
                    t.getCorridor().getPaysOrigine().getNom()
                            + " -> "
                            + t.getCorridor().getPaysDestination().getNom();

            String key = month + "_" + corridor;

            MonthlyReportDto dto =
                    map.getOrDefault(
                            key,
                            MonthlyReportDto.builder()
                                    .month(month)
                                    .corridor(corridor)
                                    .transactionCount(0L)
                                    .totalVolume(BigDecimal.ZERO)
                                    .totalRevenue(BigDecimal.ZERO)
                                    .build()
                    );

            dto.setTransactionCount(
                    dto.getTransactionCount() + 1
            );

            dto.setTotalVolume(
                    dto.getTotalVolume()
                            .add(t.getMontantEnvoye())
            );

            dto.setTotalRevenue(
                    dto.getTotalRevenue()
                            .add(t.getFrais())
            );

            map.put(key, dto);
        }

        return new ArrayList<>(map.values());
    }

    @Override
    public List<CorridorPerformanceDto> getCorridorPerformance(
            LocalDate startDate,
            LocalDate endDate
    ) {

        List<Transfert> transfers =
                transfertRepository.findByPeriod(
                        startDate.atStartOfDay(),
                        endDate.plusDays(1).atStartOfDay()
                );

        Map<String, CorridorPerformanceDto> map = new HashMap<>();

        for (Transfert t : transfers) {

            String corridor =
                    t.getCorridor().getPaysOrigine().getNom()
                            + " -> "
                            + t.getCorridor().getPaysDestination().getNom();

            CorridorPerformanceDto dto =
                    map.getOrDefault(
                            corridor,
                            CorridorPerformanceDto.builder()
                                    .corridor(corridor)
                                    .totalTransactions(0L)
                                    .totalVolume(BigDecimal.ZERO)
                                    .averageTransfer(BigDecimal.ZERO)
                                    .generatedFees(BigDecimal.ZERO)
                                    .build()
                    );

            dto.setTotalTransactions(
                    dto.getTotalTransactions() + 1
            );

            dto.setTotalVolume(
                    dto.getTotalVolume()
                            .add(t.getMontantEnvoye())
            );

            dto.setGeneratedFees(
                    dto.getGeneratedFees()
                            .add(t.getFrais())
            );

            map.put(corridor, dto);
        }

        for (CorridorPerformanceDto dto : map.values()) {

            if (dto.getTotalTransactions() > 0) {

                dto.setAverageTransfer(
                        dto.getTotalVolume().divide(
                                BigDecimal.valueOf(
                                        dto.getTotalTransactions()
                                ),
                                2,
                                java.math.RoundingMode.HALF_UP
                        )
                );
            }
        }

        return new ArrayList<>(map.values());
    }

    @Override
    public List<AlertDto> getActiveAlerts() {

        return kycAlertRepository
                .findByStatusOrderByCreatedAtDesc("OPEN")
                .stream()
                .map(alert -> AlertDto.builder()
                        .type(alert.getAlertType())
                        .message(alert.getMessage())
                        .severity(alert.getSeverity())
                        .timestamp(alert.getCreatedAt())
                        .build())
                .toList();
    }
}