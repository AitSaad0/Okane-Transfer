package com.okane.service.impl;

import com.okane.dto.converter.GrilleTarifaireConverter;
import com.okane.dto.requestDto.GrilleTarifaireRequestDTO;
import com.okane.dto.requestDto.SimulationRequestDTO;
import com.okane.dto.responseDto.GrilleTarifaireResponseDTO;
import com.okane.dto.responseDto.SimulationResponseDTO;
import com.okane.entity.Corridor;
import com.okane.entity.GrilleTarifaire;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.CorridorRepository;
import com.okane.repository.GrilleTarifaireRepository;
import com.okane.service.GrilleTarifaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GrilleTarifaireServiceImpl implements GrilleTarifaireService {

    @Autowired
    private GrilleTarifaireRepository grilleRepository;

    @Autowired
    private CorridorRepository corridorRepository;

    @Autowired
    private GrilleTarifaireConverter converter;

    @Override
    @Transactional(readOnly = true)
    public List<GrilleTarifaireResponseDTO> findAll() {
        return grilleRepository.findAll().stream()
                .map(converter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GrilleTarifaireResponseDTO findById(Long id) {
        GrilleTarifaire grille = grilleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grille tarifaire not found: " + id));
        return converter.toResponseDTO(grille);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrilleTarifaireResponseDTO> findByCorridorId(Long corridorId) {
        return grilleRepository.findByCorridorId(corridorId).stream()
                .map(converter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GrilleTarifaireResponseDTO save(GrilleTarifaireRequestDTO dto) {
        Corridor corridor = corridorRepository.findById(dto.getCorridorId())
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found: " + dto.getCorridorId()));

        GrilleTarifaire grille = converter.toEntity(dto, corridor);
        GrilleTarifaire saved = grilleRepository.save(grille);
        return converter.toResponseDTO(saved);
    }

    @Override
    public GrilleTarifaireResponseDTO update(Long id, GrilleTarifaireRequestDTO dto) {
        GrilleTarifaire grille = grilleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grille tarifaire not found: " + id));

        converter.updateEntityFromDTO(grille, dto);
        GrilleTarifaire updated = grilleRepository.save(grille);
        return converter.toResponseDTO(updated);
    }

    @Override
    public void delete(Long id) {
        GrilleTarifaire grille = grilleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grille tarifaire not found: " + id));
        grilleRepository.delete(grille);
    }

    @Override
    @Transactional(readOnly = true)
    public SimulationResponseDTO simulate(SimulationRequestDTO dto) {
        Corridor corridor = corridorRepository.findById(dto.getCorridorId())
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found: " + dto.getCorridorId()));

        GrilleTarifaire grille = grilleRepository
                .findByCorridorIdAndMontantMinLessThanEqualAndMontantMaxGreaterThanEqual(
                        dto.getCorridorId(), dto.getMontant(), dto.getMontant())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune grille tarifaire trouvée pour ce montant et ce corridor"));

        return converter.toSimulationDTO(dto.getMontant(), grille, corridor);
    }

    @Override
    public byte[] exportCsv() {
        List<GrilleTarifaire> grilles = grilleRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Corridor,Montant Min,Montant Max,Frais Fixe,Pourcentage,Part Agence\n");

        for (GrilleTarifaire g : grilles) {
            csv.append(String.format("%d,%s → %s,%s,%s,%s,%.2f%%,%.2f%%\n",
                    g.getId(),
                    g.getCorridor().getPaysOrigine().getNom(),
                    g.getCorridor().getPaysDestination().getNom(),
                    g.getMontantMin(),
                    g.getMontantMax(),
                    g.getFraisFixe(),
                    g.getPourcentageFrais(),
                    g.getPartAgence()));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportPdf() {

        return "PDF export - implement with iText library".getBytes(StandardCharsets.UTF_8);
    }
}