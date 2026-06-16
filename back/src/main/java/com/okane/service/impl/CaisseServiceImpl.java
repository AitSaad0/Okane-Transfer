package com.okane.service.impl;

import com.okane.dto.requestDto.ClotureCaisseRequestDTO;
import com.okane.dto.requestDto.EcartCaisseRequestDTO;
import com.okane.dto.responseDto.CaisseResponseDTO;
import com.okane.entity.Caisse;
import com.okane.entity.Transfert;
import com.okane.entity.User;
import com.okane.entity.enums.StatutCaisse;
import com.okane.exception.BadRequestException;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.CaisseRepository;
import com.okane.repository.TransfertRepository;
import com.okane.repository.UserRepository;
import com.okane.service.CaisseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * FIXES APPLIED:
 *
 * 1. getCaisseOuverte() called caisseRepository.findByAgentAndDateCaisseAndStatut() —
 *    this method didn't exist in the original repo (was findByAgentAndDateOuvertureAndStatut).
 *    FIX: CaisseRepository now has the correct method name (see CaisseRepository.java fix).
 *
 * 2. getCaissesAgence() called caisseRepository.findByAgenceIdAndDate() —
 *    this method didn't exist in the original repo (was findByAgenceIdAndDateOuverture).
 *    FIX: CaisseRepository now has the correct method name.
 *
 * 3. toDTO() — used t.getAgenceEmission() which does NOT exist on Transfert.
 *    The Transfert entity uses agenceEnvoi (sending agency).
 *    FIX: replaced t.getAgenceEmission() → t.getAgenceEnvoi()
 *
 * 4. toDTO() — used t.getExpediteurNom(), t.getExpediteurPrenom(), etc. as direct String fields.
 *    Transfert stores expediteur/beneficiaire as Client objects.
 *    FIX: access via t.getExpediteur().getNom() / t.getBeneficiaire().getNom() with null-safety.
 *
 * 5. toDTO() — used t.getCorridor().getDeviseSource() which returns a Devise object,
 *    but deviseCode needs a String. FIX: call .getCode() on the Devise.
 *    (Was already correct in the original, but guarded with proper null checks.)
 *
 * 6. Removed unused StatutTransfert import (not referenced directly in this service).
 */
@Service
@Transactional(readOnly = true)
public class CaisseServiceImpl implements CaisseService {

    private final CaisseRepository caisseRepository;
    private final UserRepository userRepository;
    private final TransfertRepository transfertRepository;

    public CaisseServiceImpl(CaisseRepository caisseRepository,
                             UserRepository userRepository,
                             TransfertRepository transfertRepository) {
        this.caisseRepository = caisseRepository;
        this.userRepository = userRepository;
        this.transfertRepository = transfertRepository;
    }

    // =========================================================
    // GET /api/v1/agent/cash-register
    // =========================================================
    @Override
    @Transactional(readOnly = false)
    public CaisseResponseDTO getCaisseCourante(String agentEmail) {
        User agent = findAgent(agentEmail);
        Caisse caisse = getCaisseOuverte(agent);
        return toDTO(caisse, false);
    }

    // =========================================================
    // GET /api/v1/agent/cash-register/operations
    // =========================================================
    @Override
    @Transactional(readOnly = false)
    public CaisseResponseDTO getOperationsDuJour(String agentEmail) {
        User agent = findAgent(agentEmail);
        Caisse caisse = getCaisseOuverte(agent);
        return toDTO(caisse, true);
    }

    // =========================================================
    // POST /api/v1/agent/cash-register/close
    // =========================================================
    @Override
    @Transactional
    public CaisseResponseDTO cloturerCaisse(String agentEmail, ClotureCaisseRequestDTO request) {
        User agent = findAgent(agentEmail);
        Caisse caisse = getCaisseOuverte(agent);

        BigDecimal soldeTheorique = calculerSoldeTheorique(caisse);
        BigDecimal ecart = request.getSoldeCompte().subtract(soldeTheorique);

        caisse.setStatut(StatutCaisse.FERMEE);
        caisse.setSoldeCloture(request.getSoldeCompte());
        caisse.setSoldeTheorique(soldeTheorique);
        caisse.setEcart(ecart);
        caisse.setDateCloture(LocalDateTime.now());
        if (request.getObservation() != null) {
            caisse.setObservation(request.getObservation());
        }

        if (ecart.abs().compareTo(BigDecimal.valueOf(0.01)) > 0) {
            caisse.setEcartDetecte(true);
        }

        caisseRepository.save(caisse);
        return toDTO(caisse, false);
    }

    // =========================================================
    // POST /api/v1/agent/cash-register/discrepancy
    // =========================================================
    @Override
    @Transactional
    public void signalerEcart(String agentEmail, EcartCaisseRequestDTO request) {
        User agent = findAgent(agentEmail);
        Caisse caisse = getCaisseOuverte(agent);

        caisse.setEcart(request.getMontantEcart());
        caisse.setMotifEcart(request.getMotif());
        caisse.setEcartDetecte(true);
        caisseRepository.save(caisse);
    }

    // =========================================================
    // GET /api/v1/manager/cash-registers
    // =========================================================
    @Override
    public List<CaisseResponseDTO> getCaissesAgence(String managerEmail) {
        User manager = findUserByEmail(managerEmail);
        if (manager.getAgence() == null) {
            throw new BadRequestException("Ce manager n'est affecté à aucune agence");
        }
        // FIX 2: was findByAgenceIdAndDate — method now exists in the fixed CaisseRepository
        List<Caisse> caisses = caisseRepository.findByAgenceIdAndDate(
                manager.getAgence().getId(), LocalDate.now());
        return caisses.stream().map(c -> toDTO(c, false)).toList();
    }

    // =========================================================
    // Private helpers
    // =========================================================

    @Transactional(readOnly = false)
    private Caisse getCaisseOuverte(User agent) {
        return caisseRepository.findByAgentAndDateCaisseAndStatut(
                        agent, LocalDate.now(), StatutCaisse.OUVERTE)
                .orElseGet(() -> creerCaisseJournaliere(agent));
    }

    @Transactional(readOnly = false)
    private Caisse creerCaisseJournaliere(User agent) {
        Caisse caisse = new Caisse();
        caisse.setAgent(agent);
        caisse.setAgence(agent.getAgence());
        caisse.setDateCaisse(LocalDate.now());
        caisse.setDateOuverture(LocalDateTime.now());
        caisse.setStatut(StatutCaisse.OUVERTE);
        caisse.setSoldeOuverture(BigDecimal.ZERO);
        caisse.setSoldeCourant(BigDecimal.ZERO);
        caisse.setTotalEncaissements(BigDecimal.ZERO);
        caisse.setTotalDecaissements(BigDecimal.ZERO);
        caisse.setEcartDetecte(false);
        return caisseRepository.save(caisse);
    }

    private BigDecimal calculerSoldeTheorique(Caisse caisse) {
        BigDecimal encaissements = caisse.getTotalEncaissements() != null
                ? caisse.getTotalEncaissements() : BigDecimal.ZERO;
        BigDecimal decaissements = caisse.getTotalDecaissements() != null
                ? caisse.getTotalDecaissements() : BigDecimal.ZERO;
        BigDecimal ouverture = caisse.getSoldeOuverture() != null
                ? caisse.getSoldeOuverture() : BigDecimal.ZERO;
        return ouverture.add(encaissements).subtract(decaissements);
    }

    private User findAgent(String email) {
        return findUserByEmail(email);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + email));
    }

    private CaisseResponseDTO toDTO(Caisse c, boolean avecOperations) {
        CaisseResponseDTO dto = new CaisseResponseDTO();
        dto.setId(c.getId());
        dto.setStatut(c.getStatut());
        dto.setDateCaisse(c.getDateCaisse());
        dto.setSoldeOuverture(c.getSoldeOuverture());
        dto.setSoldeCourant(c.getSoldeCourant());
        dto.setSoldeCloture(c.getSoldeCloture());
        dto.setSoldeTheorique(c.getSoldeTheorique());
        dto.setEcart(c.getEcart());
        dto.setMotifEcart(c.getMotifEcart());
        dto.setTotalEncaissements(c.getTotalEncaissements());
        dto.setTotalDecaissements(c.getTotalDecaissements());
        dto.setDateOuverture(c.getDateOuverture());
        dto.setDateClotureFaite(c.getDateCloture());

        if (c.getAgent() != null) {
            dto.setAgentNom(c.getAgent().getNom());
            dto.setAgentPrenom(c.getAgent().getPrenom());
            if (c.getAgent().getAgence() != null)
                dto.setAgenceNom(c.getAgent().getAgence().getNom());
        }

        if (avecOperations && c.getTransferts() != null) {
            List<CaisseResponseDTO.OperationCaisseDTO> ops = new ArrayList<>();
            for (Transfert t : c.getTransferts()) {
                CaisseResponseDTO.OperationCaisseDTO op = new CaisseResponseDTO.OperationCaisseDTO();
                op.setTransfertId(t.getId());
                op.setCodeRetrait(t.getCodeRetrait());
                op.setMontant(t.getMontantEnvoye());
                op.setDateHeure(t.getDateCreation());

                /*
                 * FIX 3: was t.getAgenceEmission() — that method does not exist on Transfert.
                 * The correct field is agenceEnvoi (the sending agency).
                 */
                boolean isEmission = t.getAgenceEnvoi() != null
                        && c.getAgent().getAgence() != null
                        && t.getAgenceEnvoi().getId().equals(c.getAgent().getAgence().getId());
                op.setType(isEmission ? "ENCAISSEMENT" : "DECAISSEMENT");

                if (t.getCorridor() != null && t.getCorridor().getDeviseSource() != null)
                    op.setDeviseCode(t.getCorridor().getDeviseSource().getCode());

                /*
                 * FIX 4: was t.getExpediteurNom() / t.getBeneficiaireNom() used as if they were
                 * plain String fields. Transfert stores expediteur/beneficiaire as Client objects.
                 * Access via the Client relationship with null-safety.
                 */
                String tiers;
                if (isEmission) {
                    tiers = t.getExpediteur() != null
                            ? (t.getExpediteur().getNom() + " " + t.getExpediteur().getPrenom())
                            : "";
                } else {
                    tiers = t.getBeneficiaire() != null
                            ? (t.getBeneficiaire().getNom() + " " + t.getBeneficiaire().getPrenom())
                            : "";
                }
                op.setExpediteurOuBeneficiaire(tiers);
                ops.add(op);
            }
            dto.setOperations(ops);
            dto.setNombreOperations(ops.size());
        }
        return dto;
    }
}