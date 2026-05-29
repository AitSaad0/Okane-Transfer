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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CorridorServiceImpl implements CorridorService {

    @Autowired
    private CorridorRepository corridorRepository;

    @Autowired
    private PaysRepository paysRepository;

    @Autowired
    private DeviseRepository deviseRepository;

    @Autowired
    private CorridorConverter corridorConverter;

    @Override
    @Transactional(readOnly = true)
    public List<CorridorResponseDTO> findAll() {
        return corridorRepository.findAll().stream()
                .map(corridorConverter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorridorResponseDTO> findActive() {
        return corridorRepository.findByActifTrue().stream()
                .map(corridorConverter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CorridorResponseDTO findById(Long id) {
        Corridor corridor = corridorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found with id: " + id));
        return corridorConverter.toResponseDTO(corridor);
    }

    @Override
    public CorridorResponseDTO save(CorridorRequestDTO dto) {
        Pays paysOrigine = findPaysById(dto.getPaysOrigineId(), "origine");
        Pays paysDestination = findPaysById(dto.getPaysDestinationId(), "destination");
        Devise deviseSource = findDeviseById(dto.getDeviseSourceId(), "source");
        Devise deviseDestination = findDeviseById(dto.getDeviseDestinationId(), "destination");

        Corridor corridor = corridorConverter.toEntity(dto, paysOrigine, paysDestination, deviseSource, deviseDestination);
        Corridor saved = corridorRepository.save(corridor);
        return corridorConverter.toResponseDTO(saved);
    }

    @Override
    public CorridorResponseDTO update(Long id, CorridorRequestDTO dto) {
        Corridor corridor = corridorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found with id: " + id));

        Pays paysOrigine = dto.getPaysOrigineId() != null ? findPaysById(dto.getPaysOrigineId(), "origine") : null;
        Pays paysDestination = dto.getPaysDestinationId() != null ? findPaysById(dto.getPaysDestinationId(), "destination") : null;
        Devise deviseSource = dto.getDeviseSourceId() != null ? findDeviseById(dto.getDeviseSourceId(), "source") : null;
        Devise deviseDestination = dto.getDeviseDestinationId() != null ? findDeviseById(dto.getDeviseDestinationId(), "destination") : null;

        corridorConverter.updateEntityFromDTO(corridor, dto, paysOrigine, paysDestination, deviseSource, deviseDestination);
        Corridor updated = corridorRepository.save(corridor);
        return corridorConverter.toResponseDTO(updated);
    }

    @Override
    public void delete(Long id) {
        Corridor corridor = corridorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found with id: " + id));
        corridorRepository.delete(corridor);
    }

    @Override
    public void toggleStatus(Long id) {
        Corridor corridor = corridorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found with id: " + id));
        corridor.setActif(!corridor.getActif());
        corridorRepository.save(corridor);
    }

    // Helper methods
    private Pays findPaysById(Long id, String type) {
        return paysRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pays " + type + " not found: " + id));
    }

    private Devise findDeviseById(Long id, String type) {
        return deviseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devise " + type + " not found: " + id));
    }
}