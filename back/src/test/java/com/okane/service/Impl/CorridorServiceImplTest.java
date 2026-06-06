package com.okane.service.impl;

import com.okane.dto.converter.CorridorConverter;
import com.okane.dto.requestDto.CorridorRequestDTO;
import com.okane.dto.responseDto.CorridorResponseDTO;
import com.okane.entity.Corridor;
import com.okane.entity.Devise;
import com.okane.entity.Pays;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.CorridorRepository;
import com.okane.repository.DeviseRepository;
import com.okane.repository.PaysRepository;
import com.okane.service.CorridorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorridorServiceImplTest {

    @Mock
    private CorridorRepository corridorRepository;

    @Mock
    private PaysRepository paysRepository;

    @Mock
    private DeviseRepository deviseRepository;

    @Mock
    private CorridorConverter corridorConverter;

    @InjectMocks
    private CorridorServiceImpl corridorService;

    private Corridor corridor;
    private CorridorResponseDTO responseDTO;
    private CorridorRequestDTO requestDTO;

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

        responseDTO = CorridorResponseDTO.builder()
                .id(1L)
                .tauxChange(new BigDecimal("655.957"))
                .actif(true)
                .paysOrigineNom("Sénégal")
                .paysDestinationNom("France")
                .deviseSourceCode("XOF")
                .deviseDestinationCode("EUR")
                .build();

        requestDTO = CorridorRequestDTO.builder()
                .paysOrigineId(1L)
                .paysDestinationId(2L)
                .deviseSourceId(1L)
                .deviseDestinationId(2L)
                .tauxChange(new BigDecimal("655.957"))
                .actif(true)
                .build();
    }

    @Test
    void findAll_shouldReturnList() {
        when(corridorRepository.findAll()).thenReturn(Arrays.asList(corridor));
        when(corridorConverter.toResponseDTO(corridor)).thenReturn(responseDTO);

        List<CorridorResponseDTO> result = corridorService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sénégal", result.get(0).getPaysOrigineNom());
    }

    @Test
    void findActive_shouldReturnActiveCorridors() {
        when(corridorRepository.findByActifTrue()).thenReturn(Arrays.asList(corridor));
        when(corridorConverter.toResponseDTO(corridor)).thenReturn(responseDTO);

        List<CorridorResponseDTO> result = corridorService.findActive();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActif());
    }

    @Test
    void findById_shouldReturnCorridor() {
        when(corridorRepository.findById(1L)).thenReturn(Optional.of(corridor));
        when(corridorConverter.toResponseDTO(corridor)).thenReturn(responseDTO);

        CorridorResponseDTO result = corridorService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(corridorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> corridorService.findById(99L));
    }

    @Test
    void save_shouldCreateCorridor() {
        when(paysRepository.findById(1L)).thenReturn(Optional.of(corridor.getPaysOrigine()));
        when(paysRepository.findById(2L)).thenReturn(Optional.of(corridor.getPaysDestination()));
        when(deviseRepository.findById(1L)).thenReturn(Optional.of(corridor.getDeviseSource()));
        when(deviseRepository.findById(2L)).thenReturn(Optional.of(corridor.getDeviseDestination()));
        when(corridorConverter.toEntity(any(), any(), any(), any(), any())).thenReturn(corridor);
        when(corridorRepository.save(corridor)).thenReturn(corridor);
        when(corridorConverter.toResponseDTO(corridor)).thenReturn(responseDTO);

        CorridorResponseDTO result = corridorService.save(requestDTO);

        assertNotNull(result);
        assertEquals("Sénégal", result.getPaysOrigineNom());
    }

    @Test
    void save_shouldThrow_whenPaysNotFound() {
        when(paysRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> corridorService.save(requestDTO));
    }

    @Test
    void update_shouldModifyCorridor() {
        when(corridorRepository.findById(1L)).thenReturn(Optional.of(corridor));
        when(paysRepository.findById(1L)).thenReturn(Optional.of(corridor.getPaysOrigine()));
        when(paysRepository.findById(2L)).thenReturn(Optional.of(corridor.getPaysDestination()));
        when(deviseRepository.findById(1L)).thenReturn(Optional.of(corridor.getDeviseSource()));
        when(deviseRepository.findById(2L)).thenReturn(Optional.of(corridor.getDeviseDestination()));
        when(corridorRepository.save(corridor)).thenReturn(corridor);
        when(corridorConverter.toResponseDTO(corridor)).thenReturn(responseDTO);

        CorridorResponseDTO result = corridorService.update(1L, requestDTO);

        assertNotNull(result);
        verify(corridorConverter).updateEntityFromDTO(any(), any(), any(), any(), any(), any());
    }

    @Test
    void delete_shouldRemoveCorridor() {
        when(corridorRepository.findById(1L)).thenReturn(Optional.of(corridor));
        doNothing().when(corridorRepository).delete(corridor);

        assertDoesNotThrow(() -> corridorService.delete(1L));
        verify(corridorRepository).delete(corridor);
    }

    @Test
    void toggleStatus_shouldInvertActif() {
        when(corridorRepository.findById(1L)).thenReturn(Optional.of(corridor));
        when(corridorRepository.save(corridor)).thenReturn(corridor);

        corridorService.toggleStatus(1L);

        assertFalse(corridor.getActif());
    }
}