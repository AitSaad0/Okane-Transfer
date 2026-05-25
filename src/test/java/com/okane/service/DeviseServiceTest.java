package com.okane.service;

import com.okane.entity.Devise;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeviseServiceTest {

    @Mock
    private DeviseRepository deviseRepository;

    @InjectMocks
    private DeviseService deviseService;

    private Devise mad;
    private Devise eur;
    private UUID madId;

    @BeforeEach
    void setUp() {
        madId = UUID.randomUUID();
        mad = Devise.builder()
                .id(madId)
                .code("MAD")
                .symbole("DH")
                .build();

        eur = Devise.builder()
                .id(UUID.randomUUID())
                .code("EUR")
                .symbole("€")
                .build();
    }

    @Test
    void findAll_ShouldReturnAllDevises() {
        when(deviseRepository.findAll()).thenReturn(Arrays.asList(mad, eur));

        var result = deviseService.findAll();

        assertEquals(2, result.size());
        verify(deviseRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnDevise_WhenExists() {
        when(deviseRepository.findById(madId)).thenReturn(Optional.of(mad));

        var result = deviseService.findById(madId);

        assertEquals("MAD", result.getCode());
        assertEquals("DH", result.getSymbole());
    }

    @Test
    void findById_ShouldThrowException_WhenNotExists() {
        UUID randomId = UUID.randomUUID();
        when(deviseRepository.findById(randomId)).thenReturn(Optional.empty());

        var exception = assertThrows(RuntimeException.class, () -> {
            deviseService.findById(randomId);
        });

        assertTrue(exception.getMessage().contains("Devise not found"));
    }

    @Test
    void save_ShouldSaveDevise_WhenCodeNotExists() {
        when(deviseRepository.existsByCode("USD")).thenReturn(false);
        when(deviseRepository.save(any(Devise.class))).thenAnswer(inv -> {
            Devise d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        var newDevise = Devise.builder()
                .code("USD")
                .symbole("$")
                .build();

        var result = deviseService.save(newDevise);

        assertNotNull(result.getId());
        assertEquals("USD", result.getCode());
        verify(deviseRepository).save(any(Devise.class));
    }

    @Test
    void save_ShouldThrowException_WhenCodeExists() {
        when(deviseRepository.existsByCode("MAD")).thenReturn(true);

        var duplicate = Devise.builder()
                .code("MAD")
                .symbole("DH")
                .build();

        var exception = assertThrows(RuntimeException.class, () -> {
            deviseService.save(duplicate);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(deviseRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateExistingDevise() {
        var updated = Devise.builder()
                .id(madId)
                .code("MAD")
                .symbole("MAD")
                .build();

        when(deviseRepository.findById(madId)).thenReturn(Optional.of(mad));
        when(deviseRepository.update(any(Devise.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = deviseService.update(madId, updated);

        assertEquals("MAD", result.getCode());
        assertEquals("MAD", result.getSymbole());
    }

    @Test
    void delete_ShouldCallRepositoryDelete() {
        when(deviseRepository.findById(madId)).thenReturn(Optional.of(mad));

        assertDoesNotThrow(() -> deviseService.delete(madId));
        verify(deviseRepository).deleteById(madId);
    }
}