package com.okane.service.impl;

import com.okane.entity.Pays;
import com.okane.dto.converter.PaysConverter;
import com.okane.dto.requestDto.PaysRequestDTO;
import com.okane.dto.responseDto.PaysResponseDTO;
import com.okane.repository.PaysRepository;
import com.okane.service.PaysService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaysServiceImpl implements PaysService {

    private final PaysRepository paysRepository;
    private final PaysConverter paysConverter;

    public PaysServiceImpl(PaysRepository paysRepository, PaysConverter paysConverter) {
        this.paysRepository = paysRepository;
        this.paysConverter = paysConverter;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaysResponseDTO> findAll() {
        return paysRepository.findAll()
                .stream()
                .map(paysConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaysResponseDTO findById(Long id) {
        Pays pays = paysRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pays not found: " + id));
        return paysConverter.toDTO(pays);
    }

    @Override
    @Transactional
    public PaysResponseDTO save(PaysRequestDTO dto) {
        if (paysRepository.existsByCodeIso(dto.getCodeIso())) {
            throw new RuntimeException("Pays code ISO already exists: " + dto.getCodeIso());
        }
        Pays saved = paysRepository.save(paysConverter.toEntity(dto));
        return paysConverter.toDTO(saved);
    }

    @Override
    @Transactional
    public PaysResponseDTO update(Long id, PaysRequestDTO dto) {
        Pays existing = paysRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pays not found: " + id));
        existing.setCodeIso(dto.getCodeIso());
        existing.setNom(dto.getNom());
        return paysConverter.toDTO(paysRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!paysRepository.existsById(id)) {
            throw new RuntimeException("Pays not found: " + id);
        }
        paysRepository.deleteById(id);
    }
}