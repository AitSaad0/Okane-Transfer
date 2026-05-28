package com.okane.service;

import com.okane.entity.Devise;
import com.okane.dto.requestDto.DeviseRequestDTO;
import com.okane.dto.responseDto.DeviseResponseDTO;
import com.okane.dto.converter.DeviseConverter;
import com.okane.service.impl.DeviseServiceImpl;
import com.okane.repository.DeviseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeviseServiceTest {

    @Mock
    private DeviseRepository deviseRepository;

    @Mock
    private DeviseConverter deviseConverter;

    @InjectMocks
    private DeviseServiceImpl deviseService;

    private Devise mad;
    private Devise eur;
    private DeviseResponseDTO madDTO;
    private DeviseResponseDTO eurDTO;
    private Long madId;

    @BeforeEach
    void setUp() {
        madId = 1L;

        mad = Devise.builder()
                .id(madId)
                .code("MAD")
                .symbole("DH")
                .nom("Dirham Marocain")
                .build();

        eur = Devise.builder()
                .id(2L)
                .code("EUR")
                .symbole("€")
                .nom("Euro")
                .build();

        madDTO = DeviseResponseDTO.builder()
                .id(madId)
                .code("MAD")
                .symbole("DH")
                .nom("Dirham Marocain")
                .build();

        eurDTO = DeviseResponseDTO.builder()
                .id(2L)
                .code("EUR")
                .symbole("€")
                .nom("Euro")
                .build();
    }

    @Test
    void findAll_ShouldReturnAllDevises() {
        when(deviseRepository.findAll()).thenReturn(Arrays.asList(mad, eur));
        when(deviseConverter.toDTO(mad)).thenReturn(madDTO);
        when(deviseConverter.toDTO(eur)).thenReturn(eurDTO);

        var result = deviseService.findAll();

        assertEquals(2, result.size());
        verify(deviseRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnDevise_WhenExists() {
        when(deviseRepository.findById(madId)).thenReturn(Optional.of(mad));
        when(deviseConverter.toDTO(mad)).thenReturn(madDTO);

        var result = deviseService.findById(madId);

        assertEquals("MAD", result.getCode());
        assertEquals("DH", result.getSymbole());
    }

    @Test
    void findById_ShouldThrowException_WhenNotExists() {
        Long randomId = 99L;
        when(deviseRepository.findById(randomId)).thenReturn(Optional.empty());

        var exception = assertThrows(RuntimeException.class, () ->
                deviseService.findById(randomId)
        );

        assertTrue(exception.getMessage().contains("Devise not found"));
    }

    @Test
    void save_ShouldSaveDevise_WhenCodeNotExists() {
        DeviseRequestDTO requestDTO = DeviseRequestDTO.builder()
                .code("USD")
                .symbole("$")
                .nom("Dollar")
                .build();

        Devise usd = Devise.builder()
                .id(3L)
                .code("USD")
                .symbole("$")
                .nom("Dollar")
                .build();

        DeviseResponseDTO usdDTO = DeviseResponseDTO.builder()
                .id(3L)
                .code("USD")
                .symbole("$")
                .nom("Dollar")
                .build();

        when(deviseRepository.existsByCode("USD")).thenReturn(false);
        when(deviseConverter.toEntity(requestDTO)).thenReturn(usd);
        when(deviseRepository.save(usd)).thenReturn(usd);
        when(deviseConverter.toDTO(usd)).thenReturn(usdDTO);

        var result = deviseService.save(requestDTO);

        assertNotNull(result.getId());
        assertEquals("USD", result.getCode());
        verify(deviseRepository).save(any(Devise.class));
    }

    @Test
    void save_ShouldThrowException_WhenCodeExists() {
        DeviseRequestDTO requestDTO = DeviseRequestDTO.builder()
                .code("MAD")
                .symbole("DH")
                .nom("Dirham Marocain")
                .build();

        when(deviseRepository.existsByCode("MAD")).thenReturn(true);

        var exception = assertThrows(RuntimeException.class, () ->
                deviseService.save(requestDTO)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(deviseRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateExistingDevise() {
        DeviseRequestDTO requestDTO = DeviseRequestDTO.builder()
                .code("MAD")
                .symbole("MAD")
                .nom("Dirham Marocain")
                .build();

        DeviseResponseDTO updatedDTO = DeviseResponseDTO.builder()
                .id(madId)
                .code("MAD")
                .symbole("MAD")
                .nom("Dirham Marocain")
                .build();

        when(deviseRepository.findById(madId)).thenReturn(Optional.of(mad));
        when(deviseRepository.save(mad)).thenReturn(mad);
        when(deviseConverter.toDTO(mad)).thenReturn(updatedDTO);

        var result = deviseService.update(madId, requestDTO);

        assertEquals("MAD", result.getCode());
        assertEquals("MAD", result.getSymbole());
    }

    @Test
    void delete_ShouldCallRepositoryDelete() {
        when(deviseRepository.existsById(madId)).thenReturn(true);

        assertDoesNotThrow(() -> deviseService.delete(madId));
        verify(deviseRepository).deleteById(madId);
    }
}