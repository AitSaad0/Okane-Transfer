package com.okane.service.Impl;

import com.okane.dto.converter.AgenceConverter;
import com.okane.dto.requestDto.CreateAgenceRequestDto;
import com.okane.dto.requestDto.UpdateAgenceRequestDto;
import com.okane.dto.requestDto.UpdateAgenceStatusRequestDto;
import com.okane.dto.responseDto.AgenceDashboardResponseDto;
import com.okane.dto.responseDto.AgenceResponseDto;
import com.okane.entity.Agence;
import com.okane.entity.Caisse;
import com.okane.entity.Pays;
import com.okane.entity.enums.StatutAgence;
import com.okane.entity.enums.StatutCaisse;
import com.okane.entity.enums.StatutTransfert;
import com.okane.pagination.PageResponseDto;
import com.okane.repository.AgenceRepository;
import com.okane.repository.PaysRepository;
import com.okane.service.impl.AgenceServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgenceServiceImplTest {

    @Mock private AgenceRepository agenceRepository;
    @Mock private AgenceConverter  agenceConverter;
    @Mock private PaysRepository   paysRepository;

    @InjectMocks
    private AgenceServiceImpl agenceService;

    private Pays    pays;
    private Agence  agence;
    private AgenceResponseDto agenceDto;

    @BeforeEach
    void setUp() {
        pays = Pays.builder()
                .id(1L)
                .codeIso("MAR")
                .nom("Maroc")
                .build();

        agence = Agence.builder()
                .id(1L)
                .nom("Agence Casa")
                .adresse("Rue Hassan II")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("50000.00"))
                .statut(StatutAgence.ACTIVE)
                .pays(pays)
                .build();

        agenceDto = AgenceResponseDto.builder()
                .id(1L)
                .nom("Agence Casa")
                .adresse("Rue Hassan II")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("50000.00"))
                .statut(StatutAgence.ACTIVE)
                .paysNom("Maroc")
                .paysCode("MAR")
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // getAllAgences()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getAllAgences_shouldReturnAllWhenNoFilter() {
        Page<Agence> pageResult = new PageImpl<>(List.of(agence));
        when(agenceRepository.findAll(any(Pageable.class))).thenReturn(pageResult);
        when(agenceConverter.toDto(agence)).thenReturn(agenceDto);

        PageResponseDto<AgenceResponseDto> result =
                agenceService.getAllAgences(null, null, 0, 10, "id");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Agence Casa", result.getContent().get(0).getNom());
        verify(agenceRepository).findAll(any(Pageable.class));
        verify(agenceRepository, never()).findByPaysId(any(), any());
        verify(agenceRepository, never()).findByStatut(any(), any());
        verify(agenceRepository, never()).findByPaysIdAndStatut(any(), any(), any());
    }

    @Test
    void getAllAgences_shouldFilterByPaysIdOnly() {
        Page<Agence> pageResult = new PageImpl<>(List.of(agence));
        when(agenceRepository.findByPaysId(eq(1L), any(Pageable.class))).thenReturn(pageResult);
        when(agenceConverter.toDto(agence)).thenReturn(agenceDto);

        PageResponseDto<AgenceResponseDto> result =
                agenceService.getAllAgences(1L, null, 0, 10, "id");

        assertEquals(1, result.getContent().size());
        verify(agenceRepository).findByPaysId(eq(1L), any(Pageable.class));
        verify(agenceRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAllAgences_shouldFilterByStatutOnly() {
        Page<Agence> pageResult = new PageImpl<>(List.of(agence));
        when(agenceRepository.findByStatut(eq(StatutAgence.ACTIVE), any(Pageable.class)))
                .thenReturn(pageResult);
        when(agenceConverter.toDto(agence)).thenReturn(agenceDto);

        PageResponseDto<AgenceResponseDto> result =
                agenceService.getAllAgences(null, StatutAgence.ACTIVE, 0, 10, "id");

        assertEquals(1, result.getContent().size());
        verify(agenceRepository).findByStatut(eq(StatutAgence.ACTIVE), any(Pageable.class));
        verify(agenceRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAllAgences_shouldFilterByPaysIdAndStatut() {
        Page<Agence> pageResult = new PageImpl<>(List.of(agence));
        when(agenceRepository.findByPaysIdAndStatut(eq(1L), eq(StatutAgence.ACTIVE), any(Pageable.class)))
                .thenReturn(pageResult);
        when(agenceConverter.toDto(agence)).thenReturn(agenceDto);

        PageResponseDto<AgenceResponseDto> result =
                agenceService.getAllAgences(1L, StatutAgence.ACTIVE, 0, 10, "id");

        assertEquals(1, result.getContent().size());
        verify(agenceRepository).findByPaysIdAndStatut(eq(1L), eq(StatutAgence.ACTIVE), any(Pageable.class));
    }

    @Test
    void getAllAgences_shouldReturnCorrectPaginationMetadata() {
        List<Agence> agences = List.of(agence);
        Page<Agence> pageResult = new PageImpl<>(agences, PageRequest.of(0, 10), 1L);
        when(agenceRepository.findAll(any(Pageable.class))).thenReturn(pageResult);
        when(agenceConverter.toDto(agence)).thenReturn(agenceDto);

        PageResponseDto<AgenceResponseDto> result =
                agenceService.getAllAgences(null, null, 0, 10, "id");

        assertEquals(0,    result.getPage());
        assertEquals(10,   result.getSize());
        assertEquals(1L,   result.getTotalElements());
        assertEquals(1,    result.getTotalPages());
        assertTrue(result.isLast());
    }

    @Test
    void getAllAgences_shouldReturnEmptyPageWhenNoAgences() {
        Page<Agence> emptyPage = new PageImpl<>(List.of());
        when(agenceRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PageResponseDto<AgenceResponseDto> result =
                agenceService.getAllAgences(null, null, 0, 10, "id");

        assertTrue(result.getContent().isEmpty());
        assertEquals(0L, result.getTotalElements());
    }

    // ─────────────────────────────────────────────────────────────
    // getAgenceById()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getAgenceById_shouldReturnDtoWhenFound() {
        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(agenceConverter.toDto(agence)).thenReturn(agenceDto);

        AgenceResponseDto result = agenceService.getAgenceById(1L);

        assertNotNull(result);
        assertEquals(1L,           result.getId());
        assertEquals("Agence Casa", result.getNom());
        verify(agenceRepository).findById(1L);
    }

    @Test
    void getAgenceById_shouldThrowWhenNotFound() {
        when(agenceRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> agenceService.getAgenceById(99L));

        assertTrue(ex.getMessage().contains("99"));
        verify(agenceRepository).findById(99L);
    }

    // ─────────────────────────────────────────────────────────────
    // createAgence()
    // ─────────────────────────────────────────────────────────────

    @Test
    void createAgence_shouldPersistAndReturnDto() {
        CreateAgenceRequestDto request = CreateAgenceRequestDto.builder()
                .nom("Agence Casa")
                .adresse("Rue Hassan II")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("50000.00"))
                .paysId(1L)
                .build();

        when(agenceRepository.existsByNomAndAdresse("Agence Casa", "Rue Hassan II")).thenReturn(false);
        when(paysRepository.findById(1L)).thenReturn(Optional.of(pays));
        when(agenceRepository.save(any(Agence.class))).thenReturn(agence);
        when(agenceConverter.toDto(agence)).thenReturn(agenceDto);

        AgenceResponseDto result = agenceService.createAgence(request);

        assertNotNull(result);
        assertEquals("Agence Casa", result.getNom());
        verify(agenceRepository).save(any(Agence.class));
    }

    @Test
    void createAgence_shouldSetStatutToActiveByDefault() {
        CreateAgenceRequestDto request = CreateAgenceRequestDto.builder()
                .nom("Agence Casa")
                .adresse("Rue Hassan II")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("50000.00"))
                .paysId(1L)
                .build();

        when(agenceRepository.existsByNomAndAdresse(any(), any())).thenReturn(false);
        when(paysRepository.findById(1L)).thenReturn(Optional.of(pays));
        when(agenceRepository.save(any(Agence.class))).thenAnswer(inv -> inv.getArgument(0));
        when(agenceConverter.toDto(any(Agence.class))).thenAnswer(inv -> {
            Agence a = inv.getArgument(0);
            return AgenceResponseDto.builder().statut(a.getStatut()).build();
        });

        AgenceResponseDto result = agenceService.createAgence(request);

        assertEquals(StatutAgence.ACTIVE, result.getStatut());
    }

    @Test
    void createAgence_shouldThrowWhenNomAndAdresseAlreadyExist() {
        CreateAgenceRequestDto request = CreateAgenceRequestDto.builder()
                .nom("Agence Casa")
                .adresse("Rue Hassan II")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("50000.00"))
                .paysId(1L)
                .build();

        when(agenceRepository.existsByNomAndAdresse("Agence Casa", "Rue Hassan II")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> agenceService.createAgence(request));

        assertTrue(ex.getMessage().contains("existe déjà"));
        verify(agenceRepository, never()).save(any());
    }

    @Test
    void createAgence_shouldThrowWhenPaysNotFound() {
        CreateAgenceRequestDto request = CreateAgenceRequestDto.builder()
                .nom("Agence Casa")
                .adresse("Rue Hassan II")
                .ville("Casablanca")
                .codePostal("20000")
                .plafondJournalier(new BigDecimal("50000.00"))
                .paysId(99L)
                .build();

        when(agenceRepository.existsByNomAndAdresse(any(), any())).thenReturn(false);
        when(paysRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> agenceService.createAgence(request));

        assertTrue(ex.getMessage().contains("99"));
        verify(agenceRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // updateAgence()
    // ─────────────────────────────────────────────────────────────

    @Test
    void updateAgence_shouldUpdateFieldsAndReturnDto() {
        UpdateAgenceRequestDto request = UpdateAgenceRequestDto.builder()
                .nom("Agence Rabat")
                .adresse("Rue Mohammed V")
                .ville("Rabat")
                .codePostal("10000")
                .plafondJournalier(new BigDecimal("80000.00"))
                .paysId(1L)
                .build();

        AgenceResponseDto updatedDto = AgenceResponseDto.builder()
                .id(1L)
                .nom("Agence Rabat")
                .adresse("Rue Mohammed V")
                .ville("Rabat")
                .statut(StatutAgence.ACTIVE)
                .build();

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(paysRepository.findById(1L)).thenReturn(Optional.of(pays));
        when(agenceRepository.save(any(Agence.class))).thenReturn(agence);
        when(agenceConverter.toDto(agence)).thenReturn(updatedDto);

        AgenceResponseDto result = agenceService.updateAgence(1L, request);

        assertNotNull(result);
        assertEquals("Agence Rabat", result.getNom());
        verify(agenceRepository).save(agence);
    }

    @Test
    void updateAgence_shouldThrowWhenAgenceNotFound() {
        UpdateAgenceRequestDto request = UpdateAgenceRequestDto.builder()
                .nom("X").adresse("Y").ville("Z")
                .codePostal("00000")
                .plafondJournalier(BigDecimal.TEN)
                .paysId(1L)
                .build();

        when(agenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> agenceService.updateAgence(99L, request));

        verify(agenceRepository, never()).save(any());
    }

    @Test
    void updateAgence_shouldThrowWhenPaysNotFound() {
        UpdateAgenceRequestDto request = UpdateAgenceRequestDto.builder()
                .nom("X").adresse("Y").ville("Z")
                .codePostal("00000")
                .plafondJournalier(BigDecimal.TEN)
                .paysId(99L)
                .build();

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(paysRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> agenceService.updateAgence(1L, request));

        verify(agenceRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // updateAgenceStatus()
    // ─────────────────────────────────────────────────────────────

    @Test
    void updateAgenceStatus_shouldUpdateStatutAndReturnDto() {
        UpdateAgenceStatusRequestDto request = new UpdateAgenceStatusRequestDto();
        request.setStatut(StatutAgence.SUSPENDUE);

        AgenceResponseDto suspendueDto = AgenceResponseDto.builder()
                .id(1L)
                .statut(StatutAgence.SUSPENDUE)
                .build();

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(agenceRepository.save(any(Agence.class))).thenReturn(agence);
        when(agenceConverter.toDto(agence)).thenReturn(suspendueDto);

        AgenceResponseDto result = agenceService.updateAgenceStatus(1L, request);

        assertEquals(StatutAgence.SUSPENDUE, result.getStatut());
        verify(agenceRepository).save(agence);
    }

    @Test
    void updateAgenceStatus_shouldThrowWhenAgenceNotFound() {
        UpdateAgenceStatusRequestDto request = new UpdateAgenceStatusRequestDto();
        request.setStatut(StatutAgence.SUSPENDUE);

        when(agenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> agenceService.updateAgenceStatus(99L, request));

        verify(agenceRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // getAgenceDashboard()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getAgenceDashboard_shouldThrowWhenAgenceNotFound() {
        when(agenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> agenceService.getAgenceDashboard(99L));
    }

    @Test
    void getAgenceDashboard_shouldReturnCorrectVolumesAndCounts() {
        agence.setCaisses(List.of());

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(agenceRepository.sumVolumeEnvoiJour(eq(1L), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(agenceRepository.sumVolumePaiementJour(eq(1L), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("8000.00"));
        when(agenceRepository.countTransfertsJour(eq(1L), any(LocalDateTime.class)))
                .thenReturn(5L);
        when(agenceRepository.countByStatut(1L, StatutTransfert.PAYE))     .thenReturn(8L);
        when(agenceRepository.countByStatut(1L, StatutTransfert.EN_ATTENTE)).thenReturn(2L);
        when(agenceRepository.countByStatut(1L, StatutTransfert.ANNULE))   .thenReturn(1L);
        when(agenceRepository.countByStatut(1L, StatutTransfert.EXPIRE))   .thenReturn(1L);
        when(agenceRepository.sumCommissionsGenerees(1L))
                .thenReturn(new BigDecimal("500.00"));

        AgenceDashboardResponseDto result = agenceService.getAgenceDashboard(1L);

        assertEquals(new BigDecimal("10000.00"), result.getVolumeEnvoiJour());
        assertEquals(new BigDecimal("8000.00"),  result.getVolumePaiementJour());
        assertEquals(5L,                         result.getNombreTransfertJour());
        assertEquals(8L,                         result.getTransfertsPaye());
        assertEquals(2L,                         result.getTransfertsEnAttente());
        assertEquals(1L,                         result.getTransfertsAnnule());
        assertEquals(1L,                         result.getTransfertsExpire());
        assertEquals(new BigDecimal("500.00"),   result.getCommissionsGenerees());
    }

    @Test
    void getAgenceDashboard_shouldCalculateTauxSuccesCorrectly() {
        // PAYE=8, ANNULE=1, EXPIRE=1 → totalFermes=10 → tauxSucces=80.0
        agence.setCaisses(List.of());

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(agenceRepository.sumVolumeEnvoiJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.sumVolumePaiementJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.countTransfertsJour(any(), any())).thenReturn(0L);
        when(agenceRepository.countByStatut(1L, StatutTransfert.PAYE))     .thenReturn(8L);
        when(agenceRepository.countByStatut(1L, StatutTransfert.EN_ATTENTE)).thenReturn(0L);
        when(agenceRepository.countByStatut(1L, StatutTransfert.ANNULE))   .thenReturn(1L);
        when(agenceRepository.countByStatut(1L, StatutTransfert.EXPIRE))   .thenReturn(1L);
        when(agenceRepository.sumCommissionsGenerees(1L)).thenReturn(BigDecimal.ZERO);

        AgenceDashboardResponseDto result = agenceService.getAgenceDashboard(1L);

        assertEquals(80.0, result.getTauxSucces());
    }

    @Test
    void getAgenceDashboard_shouldReturnZeroTauxSuccesWhenNoFermes() {
        agence.setCaisses(List.of());

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(agenceRepository.sumVolumeEnvoiJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.sumVolumePaiementJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.countTransfertsJour(any(), any())).thenReturn(0L);
        when(agenceRepository.countByStatut(1L, StatutTransfert.PAYE))     .thenReturn(0L);
        when(agenceRepository.countByStatut(1L, StatutTransfert.EN_ATTENTE)).thenReturn(0L);
        when(agenceRepository.countByStatut(1L, StatutTransfert.ANNULE))   .thenReturn(0L);
        when(agenceRepository.countByStatut(1L, StatutTransfert.EXPIRE))   .thenReturn(0L);
        when(agenceRepository.sumCommissionsGenerees(1L)).thenReturn(BigDecimal.ZERO);

        AgenceDashboardResponseDto result = agenceService.getAgenceDashboard(1L);

        assertEquals(0.0, result.getTauxSucces());
    }

    @Test
    void getAgenceDashboard_shouldCalculateTauxPlafondCorrectly() {
        // volumeEnvoi=10000, plafond=50000 → tauxPlafond=20.0
        agence.setCaisses(List.of());

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(agenceRepository.sumVolumeEnvoiJour(any(), any()))
                .thenReturn(new BigDecimal("10000.00"));
        when(agenceRepository.sumVolumePaiementJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.countTransfertsJour(any(), any())).thenReturn(0L);
        when(agenceRepository.countByStatut(any(), any())).thenReturn(0L);
        when(agenceRepository.sumCommissionsGenerees(1L)).thenReturn(BigDecimal.ZERO);

        AgenceDashboardResponseDto result = agenceService.getAgenceDashboard(1L);

        assertEquals(20.0, result.getTauxUtilisationPlafond());
    }

    @Test
    void getAgenceDashboard_shouldReturnZeroTauxPlafondWhenPlafondIsNull() {
        agence.setPlafondJournalier(null);
        agence.setCaisses(List.of());

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(agenceRepository.sumVolumeEnvoiJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.sumVolumePaiementJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.countTransfertsJour(any(), any())).thenReturn(0L);
        when(agenceRepository.countByStatut(any(), any())).thenReturn(0L);
        when(agenceRepository.sumCommissionsGenerees(1L)).thenReturn(BigDecimal.ZERO);

        AgenceDashboardResponseDto result = agenceService.getAgenceDashboard(1L);

        assertEquals(0.0, result.getTauxUtilisationPlafond());
    }

    @Test
    void getAgenceDashboard_shouldDetectCaisseOuverte() {
        Caisse caisseOuverte = Caisse.builder()
                .id(1L)
                .statut(StatutCaisse.OUVERTE)
                .soldeCourant(new BigDecimal("15000.00"))
                .build();
        agence.setCaisses(List.of(caisseOuverte));

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(agenceRepository.sumVolumeEnvoiJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.sumVolumePaiementJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.countTransfertsJour(any(), any())).thenReturn(0L);
        when(agenceRepository.countByStatut(any(), any())).thenReturn(0L);
        when(agenceRepository.sumCommissionsGenerees(1L)).thenReturn(BigDecimal.ZERO);

        AgenceDashboardResponseDto result = agenceService.getAgenceDashboard(1L);

        assertTrue(result.getCaisseOuverte());
        assertEquals(new BigDecimal("15000.00"), result.getSoldeCaisseActuel());
    }

    @Test
    void getAgenceDashboard_shouldReturnZeroSoldeWhenNoCaisseOuverte() {
        Caisse caisseFermee = Caisse.builder()
                .id(1L)
                .statut(StatutCaisse.FERMEE)
                .soldeCourant(new BigDecimal("15000.00"))
                .build();
        agence.setCaisses(List.of(caisseFermee));

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(agenceRepository.sumVolumeEnvoiJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.sumVolumePaiementJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.countTransfertsJour(any(), any())).thenReturn(0L);
        when(agenceRepository.countByStatut(any(), any())).thenReturn(0L);
        when(agenceRepository.sumCommissionsGenerees(1L)).thenReturn(BigDecimal.ZERO);

        AgenceDashboardResponseDto result = agenceService.getAgenceDashboard(1L);

        assertFalse(result.getCaisseOuverte());
        assertEquals(BigDecimal.ZERO, result.getSoldeCaisseActuel());
    }

    @Test
    void getAgenceDashboard_shouldReturnAgenceIdentificationFields() {
        agence.setCaisses(List.of());

        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));
        when(agenceRepository.sumVolumeEnvoiJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.sumVolumePaiementJour(any(), any())).thenReturn(BigDecimal.ZERO);
        when(agenceRepository.countTransfertsJour(any(), any())).thenReturn(0L);
        when(agenceRepository.countByStatut(any(), any())).thenReturn(0L);
        when(agenceRepository.sumCommissionsGenerees(1L)).thenReturn(BigDecimal.ZERO);

        AgenceDashboardResponseDto result = agenceService.getAgenceDashboard(1L);

        assertEquals(1L,           result.getAgenceId());
        assertEquals("Agence Casa", result.getAgenceNom());
        assertEquals("Casablanca",  result.getAgenceVille());
        assertEquals("Maroc",       result.getPaysNom());
    }
}