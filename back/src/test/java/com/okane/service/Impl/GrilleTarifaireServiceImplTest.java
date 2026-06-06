package com.okane.service.impl;

import com.okane.dto.converter.GrilleTarifaireConverter;
import com.okane.dto.requestDto.GrilleTarifaireRequestDTO;
import com.okane.dto.requestDto.SimulationRequestDTO;
import com.okane.dto.responseDto.GrilleTarifaireResponseDTO;
import com.okane.dto.responseDto.SimulationResponseDTO;
import com.okane.entity.Corridor;
import com.okane.entity.GrilleTarifaire;
import com.okane.entity.Pays;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.CorridorRepository;
import com.okane.repository.GrilleTarifaireRepository;
import com.okane.service.GrilleTarifaireService;
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
class GrilleTarifaireServiceImplTest {

    @Mock
    private GrilleTarifaireRepository grilleRepository;

    @Mock
    private CorridorRepository corridorRepository;

    @Mock
    private GrilleTarifaireConverter converter;

    @InjectMocks
    private GrilleTarifaireServiceImpl grilleTarifaireService;

    private GrilleTarifaire grille;
    private GrilleTarifaireResponseDTO responseDTO;
    private GrilleTarifaireRequestDTO requestDTO;
    private Corridor corridor;

    @BeforeEach
    void setUp() {
        Pays paysOrigine = Pays.builder().id(1L).nom("Sénégal").build();
        Pays paysDestination = Pays.builder().id(2L).nom("France").build();

        corridor = Corridor.builder()
                .id(1L)
                .paysOrigine(paysOrigine)
                .paysDestination(paysDestination)
                .build();

        grille = GrilleTarifaire.builder()
                .id(1L)
                .montantMin(new BigDecimal("0"))
                .montantMax(new BigDecimal("500"))
                .fraisFixe(new BigDecimal("25"))
                .pourcentageFrais(0.0)
                .partAgence(70.0)
                .corridor(corridor)
                .build();

        responseDTO = GrilleTarifaireResponseDTO.builder()
                .id(1L)
                .montantMin(new BigDecimal("0"))
                .montantMax(new BigDecimal("500"))
                .fraisFixe(new BigDecimal("25"))
                .pourcentageFrais(0.0)
                .partAgence(70.0)
                .corridorId(1L)
                .corridorPaysOrigineNom("Sénégal")
                .corridorPaysDestinationNom("France")
                .build();

        requestDTO = GrilleTarifaireRequestDTO.builder()
                .corridorId(1L)
                .montantMin(new BigDecimal("0"))
                .montantMax(new BigDecimal("500"))
                .fraisFixe(new BigDecimal("25"))
                .pourcentageFrais(0.0)
                .partAgence(70.0)
                .build();
    }

    @Test
    void findAll_shouldReturnList() {
        when(grilleRepository.findAll()).thenReturn(Arrays.asList(grille));
        when(converter.toResponseDTO(grille)).thenReturn(responseDTO);

        List<GrilleTarifaireResponseDTO> result = grilleTarifaireService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sénégal", result.get(0).getCorridorPaysOrigineNom());
    }

    @Test
    void findById_shouldReturnGrille() {
        when(grilleRepository.findById(1L)).thenReturn(Optional.of(grille));
        when(converter.toResponseDTO(grille)).thenReturn(responseDTO);

        GrilleTarifaireResponseDTO result = grilleTarifaireService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(grilleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> grilleTarifaireService.findById(99L));
    }

    @Test
    void findByCorridorId_shouldReturnList() {
        when(grilleRepository.findByCorridorId(1L)).thenReturn(Arrays.asList(grille));
        when(converter.toResponseDTO(grille)).thenReturn(responseDTO);

        List<GrilleTarifaireResponseDTO> result = grilleTarifaireService.findByCorridorId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void save_shouldCreateGrille() {
        when(corridorRepository.findById(1L)).thenReturn(Optional.of(corridor));
        when(converter.toEntity(requestDTO, corridor)).thenReturn(grille);
        when(grilleRepository.save(grille)).thenReturn(grille);
        when(converter.toResponseDTO(grille)).thenReturn(responseDTO);

        GrilleTarifaireResponseDTO result = grilleTarifaireService.save(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void save_shouldThrow_whenCorridorNotFound() {
        when(corridorRepository.findById(99L)).thenReturn(Optional.empty());

        GrilleTarifaireRequestDTO dto = GrilleTarifaireRequestDTO.builder()
                .corridorId(99L)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> grilleTarifaireService.save(dto));
    }

    @Test
    void update_shouldModifyGrille() {
        when(grilleRepository.findById(1L)).thenReturn(Optional.of(grille));
        when(grilleRepository.save(grille)).thenReturn(grille);
        when(converter.toResponseDTO(grille)).thenReturn(responseDTO);

        GrilleTarifaireResponseDTO result = grilleTarifaireService.update(1L, requestDTO);

        assertNotNull(result);
        verify(converter).updateEntityFromDTO(grille, requestDTO);
    }

    @Test
    void delete_shouldRemoveGrille() {
        when(grilleRepository.findById(1L)).thenReturn(Optional.of(grille));
        doNothing().when(grilleRepository).delete(grille);

        assertDoesNotThrow(() -> grilleTarifaireService.delete(1L));
        verify(grilleRepository).delete(grille);
    }

    @Test
    void simulate_shouldReturnCalculation() {
        SimulationRequestDTO simDTO = SimulationRequestDTO.builder()
                .corridorId(1L)
                .montant(new BigDecimal("400"))
                .build();

        SimulationResponseDTO simResponse = SimulationResponseDTO.builder()
                .montantEnvoye(new BigDecimal("400"))
                .fraisFixe(new BigDecimal("25"))
                .fraisTotal(new BigDecimal("25"))
                .montantRecu(new BigDecimal("375"))
                .build();

        when(corridorRepository.findById(1L)).thenReturn(Optional.of(corridor));
        when(grilleRepository.findByCorridorIdAndMontantMinLessThanEqualAndMontantMaxGreaterThanEqual(
                1L, new BigDecimal("400"), new BigDecimal("400")))
                .thenReturn(Optional.of(grille));
        when(converter.toSimulationDTO(new BigDecimal("400"), grille, corridor)).thenReturn(simResponse);

        SimulationResponseDTO result = grilleTarifaireService.simulate(simDTO);

        assertNotNull(result);
        assertEquals(new BigDecimal("25"), result.getFraisFixe());
    }

    @Test
    void simulate_shouldThrow_whenNoGrilleFound() {
        SimulationRequestDTO simDTO = SimulationRequestDTO.builder()
                .corridorId(1L)
                .montant(new BigDecimal("9999"))
                .build();

        when(corridorRepository.findById(1L)).thenReturn(Optional.of(corridor));
        when(grilleRepository.findByCorridorIdAndMontantMinLessThanEqualAndMontantMaxGreaterThanEqual(
                any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> grilleTarifaireService.simulate(simDTO));
    }

    @Test
    void exportCsv_shouldReturnBytes() {
        when(grilleRepository.findAll()).thenReturn(Arrays.asList(grille));

        byte[] result = grilleTarifaireService.exportCsv();

        assertNotNull(result);
        assertTrue(result.length > 0);
        String csv = new String(result);
        assertTrue(csv.contains("ID,Corridor,Montant Min"));
    }
}