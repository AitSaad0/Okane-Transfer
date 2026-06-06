package com.okane.service.impl;

import com.okane.dto.converter.TauxChangeConverter;
import com.okane.dto.requestDto.TauxChangeRequestDTO;
import com.okane.dto.responseDto.ConversionResponseDTO;
import com.okane.dto.responseDto.TauxChangeResponseDTO;
import com.okane.entity.Corridor;
import com.okane.entity.Devise;
import com.okane.entity.Pays;
import com.okane.entity.TauxChangeHistorique;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.CorridorRepository;
import com.okane.repository.TauxChangeHistoriqueRepository;
import com.okane.service.TauxChangeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TauxChangeServiceImplTest {

    @Mock
    private CorridorRepository corridorRepository;

    @Mock
    private TauxChangeHistoriqueRepository historiqueRepository;

    @Mock
    private TauxChangeConverter converter;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TauxChangeServiceImpl tauxChangeService;

    private Corridor corridor;
    private TauxChangeResponseDTO responseDTO;
    private TauxChangeRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        Pays paysOrigine = Pays.builder().id(1L).nom("Sénégal").build();
        Pays paysDestination = Pays.builder().id(2L).nom("France").build();
        Devise deviseSource = Devise.builder().id(1L).code("XOF").build();
        Devise deviseDestination = Devise.builder().id(2L).code("EUR").build();

        corridor = Corridor.builder()
                .id(1L)
                .tauxChange(new BigDecimal("655.957"))
                .actif(true)
                .paysOrigine(paysOrigine)
                .paysDestination(paysDestination)
                .deviseSource(deviseSource)
                .deviseDestination(deviseDestination)
                .build();

        responseDTO = TauxChangeResponseDTO.builder()
                .id(1L)
                .taux(new BigDecimal("655.957"))
                .source("ACTUEL")
                .paysOrigineNom("Sénégal")
                .paysDestinationNom("France")
                .deviseSourceCode("XOF")
                .deviseDestinationCode("EUR")
                .build();

        requestDTO = TauxChangeRequestDTO.builder()
                .tauxNouveau(new BigDecimal("660.000"))
                .source("TEST")
                .build();
    }

    @Test
    void findAllCurrentRates_shouldReturnActiveRates() {
        when(corridorRepository.findByActifTrue()).thenReturn(Arrays.asList(corridor));
        when(converter.toCurrentRateDTO(corridor)).thenReturn(responseDTO);

        List<TauxChangeResponseDTO> result = tauxChangeService.findAllCurrentRates();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sénégal", result.get(0).getPaysOrigineNom());
    }

    @Test
    void updateManual_shouldUpdateRateAndCreateHistory() {
        when(corridorRepository.findById(1L)).thenReturn(Optional.of(corridor));
        when(historiqueRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(corridorRepository.save(corridor)).thenReturn(corridor);
        when(converter.toCurrentRateDTO(corridor)).thenReturn(responseDTO);

        TauxChangeResponseDTO result = tauxChangeService.updateManual(1L, requestDTO);

        assertNotNull(result);
        verify(historiqueRepository).save(any(TauxChangeHistorique.class));
        verify(corridorRepository).save(corridor);
    }

    @Test
    void updateManual_shouldThrow_whenCorridorNotFound() {
        when(corridorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tauxChangeService.updateManual(99L, requestDTO));
    }

    @Test
    void getHistory_shouldReturnHistoryList() {
        TauxChangeHistorique historique = TauxChangeHistorique.builder()
                .id(1L)
                .tauxNouveau(new BigDecimal("660.000"))
                .source("MANUEL")
                .corridor(corridor)
                .build();

        when(historiqueRepository.findByCorridorIdOrderByDateChangementDesc(1L)).thenReturn(Arrays.asList(historique));
        when(converter.toHistoryDTO(historique)).thenReturn(responseDTO);

        List<TauxChangeResponseDTO> result = tauxChangeService.getHistory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void convert_shouldReturnConvertedAmount() {
        when(corridorRepository.findAll()).thenReturn(Arrays.asList(corridor));

        ConversionResponseDTO result = tauxChangeService.convert("XOF", "EUR", new BigDecimal("10000"));

        assertNotNull(result);
        assertEquals(new BigDecimal("10000"), result.getMontantSource());
        assertEquals("XOF", result.getDeviseSource());
        assertEquals("EUR", result.getDeviseDestination());
        assertEquals(new BigDecimal("655.957"), result.getTauxApplique());
    }

    @Test
    void convert_shouldThrow_whenCorridorNotFound() {
        when(corridorRepository.findAll()).thenReturn(Arrays.asList());

        assertThrows(ResourceNotFoundException.class, () ->
                tauxChangeService.convert("USD", "EUR", new BigDecimal("100")));
    }
}