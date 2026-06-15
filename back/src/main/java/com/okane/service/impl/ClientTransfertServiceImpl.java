package com.okane.service.impl;

import com.okane.dto.requestDto.ForceCancelRequestDTO;
import com.okane.dto.responseDto.TransfertResponseDTO;
import com.okane.entity.Client;
import com.okane.entity.Transfert;
import com.okane.entity.User;
import com.okane.entity.enums.StatutTransfert;
import com.okane.exception.BadRequestException;
import com.okane.exception.ResourceNotFoundException;
import com.okane.exception.UnauthorizedException;
import com.okane.pagination.PageResponseDto;
import com.okane.repository.TransfertRepository;
import com.okane.repository.UserRepository;
import com.okane.repository.ClientRepository;
import com.okane.service.ClientTransfertService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClientTransfertServiceImpl implements ClientTransfertService {

    private final TransfertRepository transfertRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    public ClientTransfertServiceImpl(TransfertRepository transfertRepository,
                                      UserRepository userRepository,
                                      ClientRepository clientRepository) {
        this.transfertRepository = transfertRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    // =========================================================
    // CLIENT : ses propres transferts
    // =========================================================

    @Override
    public PageResponseDto<TransfertResponseDTO> getTransfertsClient(String userEmail, Pageable pageable) {
        User user = findUserByEmail(userEmail);

        // Récupérer le client associé à cet utilisateur
        Client client = clientRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("Aucun profil client associé"));
        if (client == null) {
            throw new BadRequestException("Aucun profil client associé à cet utilisateur");
        }

        Page<Transfert> page = transfertRepository.findByClient(client, pageable);
        return buildPage(page);
    }

    @Override
    public TransfertResponseDTO getTransfertClientById(Long id, String userEmail) {
        User user = findUserByEmail(userEmail);
        Transfert t = findTransfertById(id);

        // Vérification que le transfert appartient bien au client connecté
        Client client = clientRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("Aucun profil client associé à cet utilisateur"));
        boolean isOwner = (t.getExpediteur() != null && client != null && t.getExpediteur().getId().equals(client.getId()))
                || (t.getBeneficiaire() != null && client != null && t.getBeneficiaire().getId().equals(client.getId()));

        if (!isOwner) {
            throw new UnauthorizedException("Accès refusé à ce transfert");
        }
        return toDTO(t);
    }
    @Override
    public TransfertResponseDTO trackTransfert(String codeRetrait) {
        // Endpoint public : pas de vérification d'identité
        Transfert t = transfertRepository.findByCodeRetrait(codeRetrait)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun transfert trouvé pour ce code : " + codeRetrait));
        return toPublicDTO(t);
    }

    // =========================================================
    // ADMIN : vue globale avec filtres
    // =========================================================

    @Override
    public PageResponseDto<TransfertResponseDTO> getAllTransfertsAdmin(
            String statut, Long agenceId, Long corridorId,
            String debut, String fin, Pageable pageable) {

        StatutTransfert statutEnum = null;
        if (statut != null && !statut.isEmpty()) {
            try {
                statutEnum = StatutTransfert.valueOf(statut);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Statut invalide: " + statut);
            }
        }

        LocalDateTime dtDebut = debut != null ? LocalDateTime.parse(debut, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        LocalDateTime dtFin   = fin   != null ? LocalDateTime.parse(fin,   DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;

        Page<Transfert> page = transfertRepository.findAllWithFilters(
                statutEnum, agenceId, corridorId, dtDebut, dtFin, pageable);
        return buildPage(page);
    }

    @Override
    public TransfertResponseDTO getTransfertAdminById(Long id) {
        return toDTO(findTransfertById(id));
    }

    @Override
    @Transactional
    public void forceCancelTransfert(Long id, ForceCancelRequestDTO request, String adminEmail) {
        Transfert t = findTransfertById(id);

        if (t.getStatut() == StatutTransfert.PAYE) {
            throw new BadRequestException("Impossible d'annuler un transfert déjà payé");
        }
        if (t.getStatut() == StatutTransfert.ANNULE) {
            throw new BadRequestException("Ce transfert est déjà annulé");
        }

        t.setStatut(StatutTransfert.ANNULE);
        transfertRepository.save(t);
    }

    // =========================================================
    // MANAGER : vue agence uniquement
    // =========================================================

    @Override
    public PageResponseDto<TransfertResponseDTO> getTransfertsManager(
            String managerEmail, String statut, Pageable pageable) {

        User manager = findUserByEmail(managerEmail);
        if (manager.getAgence() == null) {
            throw new BadRequestException("Ce manager n'est affecté à aucune agence");
        }

        StatutTransfert statutEnum = null;
        if (statut != null && !statut.isEmpty()) {
            try {
                statutEnum = StatutTransfert.valueOf(statut);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Statut invalide: " + statut);
            }
        }

        Page<Transfert> page = transfertRepository.findByAgence(
                manager.getAgence().getId(), statutEnum, pageable);
        return buildPage(page);
    }

    // =========================================================
    // Helpers privés
    // =========================================================

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + email));
    }

    private Transfert findTransfertById(Long id) {
        return transfertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfert introuvable : " + id));
    }

    private PageResponseDto<TransfertResponseDTO> buildPage(Page<Transfert> page) {
        List<TransfertResponseDTO> content = page.getContent().stream()
                .map(this::toDTO)
                .toList();
        PageResponseDto<TransfertResponseDTO> dto = new PageResponseDto<>();
        dto.setContent(content);
        dto.setPage(page.getNumber());
        dto.setSize(page.getSize());
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        dto.setLast(page.isLast());
        return dto;
    }

    // Mapping complet (pour admin/agent/manager)
    private TransfertResponseDTO toDTO(Transfert t) {
        TransfertResponseDTO dto = new TransfertResponseDTO();

        // Informations de base
        dto.setId(t.getId());
        dto.setCodeRetrait(t.getCodeRetrait());
        dto.setStatut(t.getStatut());
        dto.setMontantEnvoye(t.getMontantEnvoye());
        dto.setMontantRecu(t.getMontantNet()); // montantNet = montant reçu par le bénéficiaire
        dto.setFrais(t.getFrais());
        dto.setDateCreation(t.getDateCreation());
        dto.setDatePaiement(t.getDatePaiement());

        // Expéditeur (Client)
        if (t.getExpediteur() != null) {
            dto.setExpediteurNom(t.getExpediteur().getNom());
            dto.setExpediteurPrenom(t.getExpediteur().getPrenom());
            dto.setExpediteurTelephone(t.getExpediteur().getTelephone());
            dto.setExpediteurEmail(t.getExpediteur().getEmail());
        }

        // Bénéficiaire (Client)
        if (t.getBeneficiaire() != null) {
            dto.setBeneficiaireNom(t.getBeneficiaire().getNom());
            dto.setBeneficiairePrenom(t.getBeneficiaire().getPrenom());
            dto.setBeneficiaireTelephone(t.getBeneficiaire().getTelephone());
            dto.setBeneficiaireEmail(t.getBeneficiaire().getEmail());
        }

        // Corridor et devises
        if (t.getCorridor() != null) {
            dto.setCorridorId(t.getCorridor().getId());
            if (t.getCorridor().getDeviseSource() != null) {
                dto.setDeviseSource(t.getCorridor().getDeviseSource().getCode());
            }
            if (t.getCorridor().getDeviseDestination() != null) {
                dto.setDeviseDestination(t.getCorridor().getDeviseDestination().getCode());
            }
            if (t.getCorridor().getPaysOrigine() != null && t.getCorridor().getPaysDestination() != null) {
                dto.setCorridorDescription(t.getCorridor().getPaysOrigine().getNom() + " → " + t.getCorridor().getPaysDestination().getNom());
            }
        }

        // Agence d'envoi
        if (t.getAgenceEnvoi() != null) {
            dto.setAgenceId(t.getAgenceEnvoi().getId());
            dto.setAgenceNom(t.getAgenceEnvoi().getNom());
        }

        // Agent d'envoi
        if (t.getAgentEnvoi() != null) {
            dto.setAgentId(t.getAgentEnvoi().getId());
            dto.setAgentNom(t.getAgentEnvoi().getNom());
            dto.setAgentPrenom(t.getAgentEnvoi().getPrenom());
        }

        // Taux de change (si disponible dans le corridor)
        if (t.getCorridor() != null && t.getCorridor().getTauxChange() != null) {
            dto.setTauxChange(t.getCorridor().getTauxChange());
        }

        // Flag suspect
        dto.setFlagged(t.getEstSuspect() != null && t.getEstSuspect());

        return dto;
    }

    // Mapping allégé pour le suivi public (sans données sensibles)
    private TransfertResponseDTO toPublicDTO(Transfert t) {
        TransfertResponseDTO dto = new TransfertResponseDTO();

        dto.setId(t.getId());
        dto.setCodeRetrait(t.getCodeRetrait());
        dto.setStatut(t.getStatut());
        dto.setMontantRecu(t.getMontantNet());
        dto.setDateCreation(t.getDateCreation());
        dto.setDateExpiration(t.getDateCreation().plusDays(30)); // Expiration 30 jours après création

        // Masquer les détails d'identité pour le suivi public
        if (t.getBeneficiaire() != null) {
            dto.setBeneficiaireNom(maskName(t.getBeneficiaire().getNom()));
            dto.setBeneficiairePrenom(maskName(t.getBeneficiaire().getPrenom()));
        }

        // Devises
        if (t.getCorridor() != null) {
            if (t.getCorridor().getDeviseSource() != null) {
                dto.setDeviseSource(t.getCorridor().getDeviseSource().getCode());
            }
            if (t.getCorridor().getDeviseDestination() != null) {
                dto.setDeviseDestination(t.getCorridor().getDeviseDestination().getCode());
            }
            if (t.getCorridor().getPaysOrigine() != null && t.getCorridor().getPaysDestination() != null) {
                dto.setCorridorDescription(t.getCorridor().getPaysOrigine().getNom() + " → " + t.getCorridor().getPaysDestination().getNom());
            }
        }

        return dto;
    }

    private String maskName(String name) {
        if (name == null || name.length() < 2) return "***";
        return name.charAt(0) + "***";
    }
}