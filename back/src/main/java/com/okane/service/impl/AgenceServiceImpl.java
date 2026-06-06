package com.okane.service.impl;

import com.okane.entity.Pays;
import com.okane.repository.PaysRepository;
import com.okane.entity.Agence;
import com.okane.entity.Caisse;
import com.okane.dto.converter.AgenceConverter;
import com.okane.dto.requestDto.CreateAgenceRequestDto;
import com.okane.dto.requestDto.UpdateAgenceRequestDto;
import com.okane.dto.requestDto.UpdateAgenceStatusRequestDto;
import com.okane.dto.responseDto.AgenceDashboardResponseDto;
import com.okane.dto.responseDto.AgenceResponseDto;
import com.okane.pagination.PageResponseDto;
import com.okane.repository.AgenceRepository;
import com.okane.service.AgenceService;
import com.okane.entity.enums.StatutAgence;
import com.okane.entity.enums.StatutCaisse;
import com.okane.entity.enums.StatutTransfert;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgenceServiceImpl implements AgenceService {

    private final AgenceRepository agenceRepository;
    private final AgenceConverter  agenceConverter;
    private final PaysRepository   paysRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AgenceResponseDto> getAllAgences(
            Long paysId, StatutAgence statut, int page, int size, String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<Agence> agencePage;

        if (paysId != null && statut != null) {
            agencePage = agenceRepository.findByPaysIdAndStatut(paysId, statut, pageable);
        } else if (paysId != null) {
            agencePage = agenceRepository.findByPaysId(paysId, pageable);
        } else if (statut != null) {
            agencePage = agenceRepository.findByStatut(statut, pageable);
        } else {
            agencePage = agenceRepository.findAll(pageable);
        }

        List<AgenceResponseDto> content = agencePage.getContent()
                .stream()
                .map(agenceConverter::toDto)
                .toList();

        return new PageResponseDto<>(
                content,
                agencePage.getNumber(),
                agencePage.getSize(),
                agencePage.getTotalElements(),
                agencePage.getTotalPages(),
                agencePage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AgenceResponseDto getAgenceById(Long id) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agence introuvable avec l'id : " + id));
        return agenceConverter.toDto(agence);
    }

    @Override
    @Transactional
    public AgenceResponseDto createAgence(CreateAgenceRequestDto request) {
        if (agenceRepository.existsByNomAndAdresse(request.getNom(), request.getAdresse())) {
            throw new IllegalArgumentException("Une agence avec ce nom et cette adresse existe déjà");
        }

        Pays pays = paysRepository.findById(request.getPaysId())
                .orElseThrow(() -> new EntityNotFoundException("Pays introuvable avec l'id : " + request.getPaysId()));

        Agence agence = Agence.builder()
                .nom(request.getNom())
                .adresse(request.getAdresse())
                .ville(request.getVille())
                .codePostal(request.getCodePostal())
                .plafondJournalier(request.getPlafondJournalier())
                .statut(StatutAgence.ACTIVE)
                .pays(pays)
                .build();

        return agenceConverter.toDto(agenceRepository.save(agence));
    }

    @Override
    @Transactional
    public AgenceResponseDto updateAgence(Long id, UpdateAgenceRequestDto request) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agence introuvable avec l'id : " + id));

        Pays pays = paysRepository.findById(request.getPaysId())
                .orElseThrow(() -> new EntityNotFoundException("Pays introuvable avec l'id : " + request.getPaysId()));

        agence.setNom(request.getNom());
        agence.setAdresse(request.getAdresse());
        agence.setVille(request.getVille());
        agence.setCodePostal(request.getCodePostal());
        agence.setPlafondJournalier(request.getPlafondJournalier());
        agence.setPays(pays);

        return agenceConverter.toDto(agenceRepository.save(agence));
    }

    @Override
    @Transactional
    public AgenceResponseDto updateAgenceStatus(Long id, UpdateAgenceStatusRequestDto request) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agence introuvable avec l'id : " + id));

        agence.setStatut(request.getStatut());
        return agenceConverter.toDto(agenceRepository.save(agence));
    }

    @Override
    @Transactional(readOnly = true)
    public AgenceDashboardResponseDto getAgenceDashboard(Long id) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agence introuvable avec l'id : " + id));

        LocalDateTime debutJour = LocalDate.now().atStartOfDay();

        // Volumes
        BigDecimal volumeEnvoi    = agenceRepository.sumVolumeEnvoiJour(id, debutJour);
        BigDecimal volumePaiement = agenceRepository.sumVolumePaiementJour(id, debutJour);
        Long       nbJour         = agenceRepository.countTransfertsJour(id, debutJour);

        // Comptages par statut
        long paye      = agenceRepository.countByStatut(id, StatutTransfert.PAYE);
        long enAttente = agenceRepository.countByStatut(id, StatutTransfert.EN_ATTENTE);
        long annule    = agenceRepository.countByStatut(id, StatutTransfert.ANNULE);
        long expire    = agenceRepository.countByStatut(id, StatutTransfert.EXPIRE);

        // Taux de succès = PAYE / (PAYE + ANNULE + EXPIRE) * 100
        long   totalFermes = paye + annule + expire;
        double tauxSucces  = totalFermes > 0
                ? Math.round((double) paye / totalFermes * 10000.0) / 100.0
                : 0.0;

        // Commissions
        BigDecimal commissions = agenceRepository.sumCommissionsGenerees(id);

        // Taux utilisation plafond
        BigDecimal plafond    = agence.getPlafondJournalier();
        double     tauxPlafond = plafond != null && plafond.compareTo(BigDecimal.ZERO) > 0
                ? volumeEnvoi
                .divide(plafond, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        // Caisse active
        Caisse caisseActive = agence.getCaisses() != null
                ? agence.getCaisses().stream()
                .filter(c -> c.getStatut() == StatutCaisse.OUVERTE)
                .findFirst()
                .orElse(null)
                : null;

        return AgenceDashboardResponseDto.builder()
                .agenceId(agence.getId())
                .agenceNom(agence.getNom())
                .agenceVille(agence.getVille())
                .paysNom(agence.getPays() != null ? agence.getPays().getNom() : null)
                .volumeEnvoiJour(volumeEnvoi)
                .volumePaiementJour(volumePaiement)
                .nombreTransfertJour(nbJour)
                .tauxSucces(tauxSucces)
                .transfertsPaye(paye)
                .transfertsEnAttente(enAttente)
                .transfertsAnnule(annule)
                .transfertsExpire(expire)
                .commissionsGenerees(commissions)
                .plafondJournalier(plafond)
                .tauxUtilisationPlafond(tauxPlafond)
                .soldeCaisseActuel(caisseActive != null ? caisseActive.getSoldeCourant() : BigDecimal.ZERO)
                .caisseOuverte(caisseActive != null)
                .build();
    }
}