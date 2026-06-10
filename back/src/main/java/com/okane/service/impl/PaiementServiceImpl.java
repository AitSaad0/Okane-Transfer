package com.okane.service.impl;

import com.okane.dto.converter.PaiementConverter;
import com.okane.dto.requestDto.PaiementRequestDTO;
import com.okane.dto.responseDto.PaiementResponseDTO;
import com.okane.entity.Client;
import com.okane.entity.Transfert;
import com.okane.entity.User;
import com.okane.entity.enums.CanalNotification;
import com.okane.entity.enums.StatutTransfert;
import com.okane.entity.enums.TypeNotification;
import com.okane.exception.BadRequestException;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.ClientRepository;
import com.okane.repository.TransfertRepository;
import com.okane.repository.UserRepository;
import com.okane.service.NotificationService;
import com.okane.service.PaiementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaiementServiceImpl implements PaiementService {

    @Autowired
    private TransfertRepository transfertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PaiementConverter paiementConverter;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public PaiementResponseDTO rechercherParCodeRetrait(String codeRetrait) {
        Transfert transfert = transfertRepository.findByCodeRetrait(codeRetrait)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun transfert trouvé avec ce code retrait"));
        return paiementConverter.toPaiementResponseDTO(transfert);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaiementResponseDTO> rechercherParTelephoneBeneficiaire(String telephone) {
        List<Transfert> transferts = transfertRepository.findByBeneficiaireTelephone(telephone)
                .stream()
                .filter(t -> t.getStatut() == StatutTransfert.EN_ATTENTE)
                .toList();

        if (transferts.isEmpty()) {
            throw new ResourceNotFoundException("Aucun transfert en attente trouvé pour ce numéro de téléphone");
        }
        return transferts.stream()
                .map(paiementConverter::toPaiementResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public PaiementResponseDTO payerTransfert(PaiementRequestDTO request, String agentEmail) {
        Transfert transfert = transfertRepository.findById(request.getTransfertId())
                .orElseThrow(() -> new ResourceNotFoundException("Transfert introuvable"));

        if (transfert.getStatut() != StatutTransfert.EN_ATTENTE) {
            throw new BadRequestException("Ce transfert n'est pas en attente de paiement (statut actuel: "
                    + transfert.getStatut().name() + ")");
        }

        if (!transfert.getCodeRetrait().equals(request.getCodeRetrait())) {
            throw new BadRequestException("Code retrait incorrect");
        }

        Client beneficiaire = transfert.getBeneficiaire();
        if (beneficiaire == null) {
            throw new BadRequestException("Bénéficiaire introuvable");
        }

        String storedPiece = beneficiaire.getNumPieceIdentite();
        String enteredPiece = request.getPieceIdentiteBeneficiaire();

        if (storedPiece != null && storedPiece.startsWith("TEMP-")) {
            clientRepository.findByNumPieceIdentite(enteredPiece).ifPresentOrElse(
                existingClient -> transfert.setBeneficiaire(existingClient),
                () -> {
                    beneficiaire.setNumPieceIdentite(enteredPiece);
                    clientRepository.save(beneficiaire);
                }
            );
        } else if (!enteredPiece.equals(storedPiece)) {
            throw new BadRequestException("La pièce d'identité fournie ne correspond pas au bénéficiaire");
        }

        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + agentEmail));

        transfert.setStatut(StatutTransfert.PAYE);
        transfert.setDatePaiement(LocalDateTime.now());
        transfert.setAgentPaiement(agent);
        transfert.setAgencePaiement(agent.getAgence());

        Transfert saved = transfertRepository.save(transfert);

        try {
            notificationService.sendTransferNotification(
                    agent, saved, TypeNotification.CONFIRMATION_RETRAIT,
                    CanalNotification.EMAIL,
                    "Paiement confirmé. Code retrait: " + saved.getCodeRetrait()
            );
        } catch (Exception e) {
        }

        return paiementConverter.toPaiementResponseDTO(saved);
    }
}
