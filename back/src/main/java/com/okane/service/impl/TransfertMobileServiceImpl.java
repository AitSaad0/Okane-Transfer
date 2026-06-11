package com.okane.service.impl;

import com.okane.dto.converter.TransfertMobileConverter;
import com.okane.dto.requestDto.TransfertMobileRequestDTO;
import com.okane.dto.responseDto.TransfertMobileResponseDTO;
import com.okane.entity.*;
import com.okane.entity.enums.CanalNotification;
import com.okane.entity.enums.StatutTransfert;
import com.okane.entity.enums.TypeNotification;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.*;
import com.okane.service.EmailService;
import com.okane.service.NotificationService;
import com.okane.service.SmsService;
import com.okane.service.TransfertMobileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class TransfertMobileServiceImpl implements TransfertMobileService {

    private static final String REF_PREFIX = "MOM-";
    private static final int REF_SUFFIX_LENGTH = 8;
    private static final String REF_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private TransfertRepository transfertRepository;

    @Autowired
    private TransfertMobileRepository transfertMobileRepository;

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
    private TransfertMobileConverter converter;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Override
    public TransfertMobileResponseDTO creerTransfertMobile(TransfertMobileRequestDTO request, String agentEmail) {
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
                        "Aucune grille tarifaire trouv\u00e9e pour ce montant et ce corridor"));

        BigDecimal fraisVariable = request.getMontantEnvoye()
                .multiply(BigDecimal.valueOf(grille.getPourcentageFrais()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal fraisFixe = grille.getFraisFixe();
        BigDecimal fraisTotal = fraisFixe.add(fraisVariable);
        BigDecimal montantNet = request.getMontantEnvoye().subtract(fraisTotal);

        String reference = genererReference();

        Transfert transfert = Transfert.builder()
                .codeRetrait(reference)
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

        Transfert savedTransfert = transfertRepository.save(transfert);

        TransfertMobile mobile = TransfertMobile.builder()
                .operateur(request.getOperateur())
                .numeroDestinataire(request.getBeneficiaire().getTelephone())
                .transfert(savedTransfert)
                .build();

        transfertMobileRepository.save(mobile);

        CompletableFuture.runAsync(() -> envoyerNotifications(agent, savedTransfert, expediteur, beneficiaire));

        return converter.toResponseDTO(savedTransfert, mobile, fraisFixe, fraisVariable);
    }

    private Client findOrCreateClient(TransfertMobileRequestDTO.InfoPersonne info) {
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

    private String genererReference() {
        String ref;
        int maxAttempts = 100;
        int attempts = 0;
        do {
            StringBuilder sb = new StringBuilder(REF_SUFFIX_LENGTH);
            for (int i = 0; i < REF_SUFFIX_LENGTH; i++) {
                sb.append(REF_CHARS.charAt(RANDOM.nextInt(REF_CHARS.length())));
            }
            ref = REF_PREFIX + sb;
            attempts++;
            if (attempts > maxAttempts) {
                throw new RuntimeException("Impossible de g\u00e9n\u00e9rer une r\u00e9f\u00e9rence unique");
            }
        } while (transfertRepository.existsByCodeRetrait(ref));
        return ref;
    }

    private void envoyerNotifications(User agent, Transfert transfert, Client expediteur, Client beneficiaire) {
        notificationService.sendTransferNotification(
                agent, transfert, TypeNotification.TRANSFERT_CREE,
                CanalNotification.EMAIL,
                "Transfert mobile money cr\u00e9\u00e9 avec succ\u00e8s. R\u00e9f\u00e9rence: " + transfert.getCodeRetrait()
        );

        String messageSmsExpediteur = "OKANE: Votre transfert mobile money de " + transfert.getMontantEnvoye()
                + " a \u00e9t\u00e9 cr\u00e9\u00e9. R\u00e9f\u00e9rence: " + transfert.getCodeRetrait();
        String messageSmsBeneficiaire = "OKANE: Vous avez re\u00e7u un transfert mobile money de "
                + transfert.getMontantEnvoye() + ". R\u00e9f\u00e9rence: " + transfert.getCodeRetrait();

        try {
            smsService.sendSms(expediteur.getTelephone(), messageSmsExpediteur);
        } catch (Exception e) {
        }

        try {
            smsService.sendSms(beneficiaire.getTelephone(), messageSmsBeneficiaire);
        } catch (Exception e) {
        }

        try {
            emailService.send(expediteur.getEmail(),
                    "Transfert mobile money Okane cr\u00e9\u00e9",
                    "Bonjour " + expediteur.getPrenom() + ",\n\n"
                            + "Votre transfert mobile money de " + transfert.getMontantEnvoye() + " a \u00e9t\u00e9 cr\u00e9\u00e9 avec succ\u00e8s.\n"
                            + "R\u00e9f\u00e9rence: " + transfert.getCodeRetrait() + "\n"
                            + "Montant net: " + transfert.getMontantNet() + "\n\n"
                            + "Merci de votre confiance.\n"
                            + "L'\u00e9quipe OkaneTransfer");
        } catch (Exception e) {
        }
    }
}
