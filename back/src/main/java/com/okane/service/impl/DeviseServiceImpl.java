package com.okane.service.impl;

import com.okane.entity.Devise;
import com.okane.dto.converter.DeviseConverter;
import com.okane.dto.requestDto.DeviseRequestDTO;
import com.okane.dto.responseDto.DeviseResponseDTO;
import com.okane.repository.DeviseRepository;
import com.okane.service.DeviseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviseServiceImpl implements DeviseService {

    private final DeviseRepository deviseRepository;
    private final DeviseConverter deviseConverter;

    public DeviseServiceImpl(DeviseRepository deviseRepository, DeviseConverter deviseConverter) {
        this.deviseRepository = deviseRepository;
        this.deviseConverter = deviseConverter;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviseResponseDTO> findAll() {
        return deviseRepository.findAll()
                .stream()
                .map(deviseConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeviseResponseDTO findById(Long id) {
        Devise devise = deviseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devise not found: " + id));
        return deviseConverter.toDTO(devise);
    }

    @Override
    @Transactional
    public DeviseResponseDTO save(DeviseRequestDTO dto) {
        if (deviseRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Currency code already exists: " + dto.getCode());
        }
        Devise saved = deviseRepository.save(deviseConverter.toEntity(dto));
        return deviseConverter.toDTO(saved);
    }

    @Override
    @Transactional
    public DeviseResponseDTO update(Long id, DeviseRequestDTO dto) {
        Devise existing = deviseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devise not found: " + id));
        existing.setCode(dto.getCode());
        existing.setSymbole(dto.getSymbole());
        existing.setNom(dto.getNom());
        return deviseConverter.toDTO(deviseRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!deviseRepository.existsById(id)) {
            throw new RuntimeException("Devise not found: " + id);
        }
        deviseRepository.deleteById(id);
    }
}