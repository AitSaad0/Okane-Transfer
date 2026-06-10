package com.okane.service.impl;

import com.okane.dto.converter.TransfertConverter;
import com.okane.dto.requestDto.TransfertRequestDTO;
import com.okane.dto.responseDto.TransfertResponseDTO;
import com.okane.entity.*;
import com.okane.entity.enums.CanalNotification;
import com.okane.entity.enums.StatutTransfert;
import com.okane.entity.enums.TypeNotification;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.*;
import com.okane.service.EmailService;
import com.okane.service.NotificationService;
import com.okane.service.SmsService;
import com.okane.service.TransfertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Transactional
public class TransfertServiceImpl implements TransfertService {

    private static final String CODE_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private TransfertRepository transfertRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CorridorRepository corridorRepository;

    @Autowired
    private GrilleTarifaireRepository grilleTarifaireRepository;

    @Autowired
    private PaysRepository paysRepository;

    @Autowired
    private TransfertConverter transfertConverter;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Override
    public TransfertResponseDTO creerTransfert(TransfertRequestDTO request, String agentEmail) {
        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + agentEmail));

        Client expediteur = findOrCreateClient(request.getExpediteur());
        Client beneficiaire = findOrCreateClient(request.getBeneficiaire());

        Corridor corridor = corridorRepository.findById(request.getCorridorId())
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found: " + request.getCorridorId()));

        GrilleTarifaire grille = grilleTarifaireRepository
                .findByCorridorIdAndMontantMinLessThanEqualAndMontantMaxGreaterThanEqual(
                        request.getCorridorId(), request.getMontantEnvoye(), request.getMontantEnvoye())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune grille tarifaire trouvée pour ce montant et ce corridor"));

        BigDecimal fraisVariable = request.getMontantEnvoye()
                .multiply(BigDecimal.valueOf(grille.getPourcentageFrais()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal fraisTotal = grille.getFraisFixe().add(fraisVariable);
        BigDecimal montantNet = request.getMontantEnvoye().subtract(fraisTotal);

        String codeRetrait = genererCodeRetrait();

        Transfert transfert = Transfert.builder()
                .codeRetrait(codeRetrait)
                .montantEnvoye(request.getMontantEnvoye())
                .frais(fraisTotal)
                .montantNet(montantNet)
                .statut(StatutTransfert.EN_ATTENTE)
                .estSuspect(false)
                .dateCreation(LocalDateTime.now())
                .expediteur(expediteur)
                .beneficiaire(beneficiaire)
                .corridor(corridor)
                .agenceEnvoi(agent.getAgence())
                .agentEnvoi(agent)
                .build();

        Transfert saved = transfertRepository.save(transfert);

        envoyerNotifications(agent, saved, expediteur);

        return transfertConverter.toResponseDTO(saved);
    }

    private Client findOrCreateClient(TransfertRequestDTO.InfoPersonne info) {
        if (info.getNumPieceIdentite() != null
                && clientRepository.existsByNumPieceIdentite(info.getNumPieceIdentite())) {
            return clientRepository.findByNumPieceIdentite(info.getNumPieceIdentite())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found by CIN"));
        }

        Pays pays = paysRepository.findById(info.getPaysId())
                .orElseThrow(() -> new ResourceNotFoundException("Pays not found: " + info.getPaysId()));

        String email = info.getEmail() != null ? info.getEmail() : info.getTelephone() + "@okane.local";

        return clientRepository.save(Client.builder()
                .nom(info.getNom())
                .prenom(info.getPrenom())
                .numPieceIdentite(info.getNumPieceIdentite() != null ? info.getNumPieceIdentite() : "TEMP-" + System.currentTimeMillis())
                .telephone(info.getTelephone())
                .email(email)
                .pays(pays)
                .estSurListeSurveillance(false)
                .deleted(false)
                .build());
    }

    private String genererCodeRetrait() {
        String code;
        int maxAttempts = 100;
        int attempts = 0;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
            attempts++;
            if (attempts > maxAttempts) {
                throw new RuntimeException("Impossible de générer un code retrait unique");
            }
        } while (transfertRepository.existsByCodeRetrait(code));
        return code;
    }

    private void envoyerNotifications(User agent, Transfert transfert, Client expediteur) {
        notificationService.sendTransferNotification(
                agent, transfert, TypeNotification.TRANSFERT_CREE,
                CanalNotification.EMAIL,
                "Transfert créé avec succès. Code de retrait: " + transfert.getCodeRetrait()
        );

        String messageSms = "OKANE: Votre transfert de " + transfert.getMontantEnvoye()
                + " a été créé. Code de retrait: " + transfert.getCodeRetrait();
        try {
            smsService.sendSms(expediteur.getTelephone(), messageSms);
        } catch (Exception e) {
        }

        try {
            emailService.send(expediteur.getEmail(),
                    "Transfert Okane créé",
                    "Bonjour " + expediteur.getPrenom() + ",\n\n"
                            + "Votre transfert de " + transfert.getMontantEnvoye() + " a été créé avec succès.\n"
                            + "Code de retrait: " + transfert.getCodeRetrait() + "\n"
                            + "Montant net: " + transfert.getMontantNet() + "\n\n"
                            + "Merci de votre confiance.\n"
                            + "L'équipe OkaneTransfer");
        } catch (Exception e) {
        }
    }
}
