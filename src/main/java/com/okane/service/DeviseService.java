package com.okane.service;

import com.okane.entity.Devise;
import com.okane.repository.DeviseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DeviseService {

    private final DeviseRepository deviseRepository;

    public DeviseService(DeviseRepository deviseRepository) {
        this.deviseRepository = deviseRepository;
    }

    @Transactional(readOnly = true)
    public List<Devise> findAll() {
        return deviseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Devise findById(UUID id) {
        return deviseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devise not found: " + id));
    }

    @Transactional
    public Devise save(Devise devise) {
        if (deviseRepository.existsByCode(devise.getCode())) {
            throw new RuntimeException("Currency code already exists: " + devise.getCode());
        }
        return deviseRepository.save(devise);
    }

    @Transactional
    public Devise update(UUID id, Devise updatedDevise) {
        Devise existing = findById(id);
        existing.setCode(updatedDevise.getCode());
        existing.setSymbole(updatedDevise.getSymbole());
        return deviseRepository.update(existing);
    }

    @Transactional
    public void delete(UUID id) {
        deviseRepository.deleteById(id);
    }
}