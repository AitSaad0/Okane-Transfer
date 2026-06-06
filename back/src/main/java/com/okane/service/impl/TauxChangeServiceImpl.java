package com.okane.service.impl;

import com.okane.dto.converter.TauxChangeConverter;
import com.okane.dto.external.ExchangeRateApiResponse;
import com.okane.dto.requestDto.TauxChangeRequestDTO;
import com.okane.dto.responseDto.ConversionResponseDTO;
import com.okane.dto.responseDto.TauxChangeResponseDTO;
import com.okane.entity.Corridor;
import com.okane.entity.TauxChangeHistorique;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.CorridorRepository;
import com.okane.repository.TauxChangeHistoriqueRepository;
import com.okane.service.TauxChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TauxChangeServiceImpl implements TauxChangeService {

    @Autowired
    private CorridorRepository corridorRepository;

    @Autowired
    private TauxChangeHistoriqueRepository historiqueRepository;

    @Autowired
    private TauxChangeConverter converter;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${exchange.api.url:https://api.exchangerate-api.com/v4/latest/}")
    private String apiBaseUrl;

    @Override
    @Transactional(readOnly = true)
    public List<TauxChangeResponseDTO> findAllCurrentRates() {
        return corridorRepository.findByActifTrue().stream()
                .map(converter::toCurrentRateDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TauxChangeResponseDTO updateManual(Long corridorId, TauxChangeRequestDTO dto) {
        Corridor corridor = corridorRepository.findById(corridorId)
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found: " + corridorId));

        TauxChangeHistorique historique = TauxChangeHistorique.builder()
                .corridor(corridor)
                .tauxAncien(corridor.getTauxChange())
                .tauxNouveau(dto.getTauxNouveau())
                .source(dto.getSource() != null ? dto.getSource() : "MANUEL")
                .dateChangement(LocalDateTime.now())
                .build();
        historiqueRepository.save(historique);

        corridor.setTauxChange(dto.getTauxNouveau());
        corridorRepository.save(corridor);

        return converter.toCurrentRateDTO(corridor);
    }

    @Override
    public void syncFromExternalApi() {
        List<Corridor> corridors = corridorRepository.findByActifTrue();

        for (Corridor corridor : corridors) {
            try {
                String fromCode = corridor.getDeviseSource().getCode();
                String toCode = corridor.getDeviseDestination().getCode();

                String url = apiBaseUrl + fromCode;
                ExchangeRateApiResponse response = restTemplate.getForObject(url, ExchangeRateApiResponse.class);

                if (response != null && response.getRates() != null && response.getRates().containsKey(toCode)) {
                    BigDecimal newRate = response.getRates().get(toCode);

                    TauxChangeHistorique historique = TauxChangeHistorique.builder()
                            .corridor(corridor)
                            .tauxAncien(corridor.getTauxChange())
                            .tauxNouveau(newRate)
                            .source("API_EXTERNE")
                            .dateChangement(LocalDateTime.now())
                            .build();
                    historiqueRepository.save(historique);

                    corridor.setTauxChange(newRate);
                    corridorRepository.save(corridor);
                }
            } catch (Exception e) {
                System.err.println("Erreur sync corridor " + corridor.getId() + ": " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TauxChangeResponseDTO> getHistory(Long corridorId) {
        return historiqueRepository.findByCorridorIdOrderByDateChangementDesc(corridorId).stream()
                .map(converter::toHistoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConversionResponseDTO convert(String from, String to, BigDecimal amount) {
        Corridor corridor = corridorRepository.findAll().stream()
                .filter(c -> c.getDeviseSource().getCode().equals(from)
                        && c.getDeviseDestination().getCode().equals(to)
                        && c.getActif())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Corridor not found for " + from + " -> " + to));

        BigDecimal taux = corridor.getTauxChange();
        BigDecimal converted = amount.multiply(taux).setScale(2, RoundingMode.HALF_UP);

        return ConversionResponseDTO.builder()
                .montantSource(amount)
                .deviseSource(from)
                .montantConverti(converted)
                .deviseDestination(to)
                .tauxApplique(taux)
                .message("Taux figé au moment de la conversion")
                .build();
    }
}