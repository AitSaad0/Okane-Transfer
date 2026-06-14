package com.okane.service.impl;

import com.okane.dto.responseDto.ManagerDashboardResponseDTO;
import com.okane.entity.Agence;
import com.okane.entity.Caisse;
import com.okane.entity.Transfert;
import com.okane.entity.User;
import com.okane.entity.enums.StatutCaisse;
import com.okane.entity.enums.StatutTransfert;
import com.okane.exception.BadRequestException;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.CaisseRepository;
import com.okane.repository.TransfertRepository;
import com.okane.repository.UserRepository;
import com.okane.service.ManagerDashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ManagerDashboardServiceImpl implements ManagerDashboardService {

    private final UserRepository userRepository;
    private final TransfertRepository transfertRepository;
    private final CaisseRepository caisseRepository;

    public ManagerDashboardServiceImpl(UserRepository userRepository,
                                       TransfertRepository transfertRepository,
                                       CaisseRepository caisseRepository) {
        this.userRepository = userRepository;
        this.transfertRepository = transfertRepository;
        this.caisseRepository = caisseRepository;
    }

    // =========================================================
    // GET /api/v1/manager/dashboard
    // =========================================================
    @Override
    public ManagerDashboardResponseDTO getDashboard(String managerEmail) {
        User manager = findManager(managerEmail);
        Long agenceId = manager.getAgence().getId();
        LocalDate today = LocalDate.now();
        return buildDashboard(manager, agenceId, today);
    }

    // =========================================================
    // GET /api/v1/manager/reports/daily
    // =========================================================
    @Override
    public ManagerDashboardResponseDTO getRapportJournalier(String managerEmail, String date) {
        User manager = findManager(managerEmail);
        Long agenceId = manager.getAgence().getId();
        LocalDate targetDate = date != null && !date.isEmpty()
                ? LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now();
        return buildDashboard(manager, agenceId, targetDate);
    }

    // =========================================================
    // GET /api/v1/manager/reports/export
    // =========================================================
    @Override
    public byte[] exportRapport(String managerEmail, String format, String date) {
        ManagerDashboardResponseDTO data = getRapportJournalier(managerEmail, date);

        if ("csv".equalsIgnoreCase(format)) {
            return buildCsvBytes(data);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return buildPdfBytes(data);
        }

        throw new BadRequestException("Format non supporté. Utilisez 'csv' ou 'pdf'.");
    }

    // =========================================================
    // Construction du dashboard
    // =========================================================
    private ManagerDashboardResponseDTO buildDashboard(User manager, Long agenceId, LocalDate date) {
        LocalDateTime debut = date.atStartOfDay();
        LocalDateTime fin   = date.atTime(LocalTime.MAX);

        // Récupérer tous les transferts du jour pour cette agence
        List<Transfert> transfertsJour = transfertRepository
                .findAllWithFilters(null, agenceId, null, debut, fin, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        long total = transfertsJour.size();
        long payes = transfertsJour.stream()
                .filter(t -> t.getStatut() == StatutTransfert.PAYE)
                .count();
        long enAttente = transfertsJour.stream()
                .filter(t -> t.getStatut() == StatutTransfert.EN_ATTENTE)
                .count();

        BigDecimal volumeJour = transfertsJour.stream()
                .filter(t -> t.getStatut() == StatutTransfert.PAYE)
                .map(Transfert::getMontantEnvoye)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal commissionsJour = transfertsJour.stream()
                .filter(t -> t.getStatut() == StatutTransfert.PAYE)
                .map(Transfert::getFrais)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double tauxSucces = total > 0
                ? BigDecimal.valueOf((double) payes / total * 100)
                .setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // Caisses ouvertes du jour - Correction: utiliser findByAgenceIdAndDateOuverture
        List<Caisse> caissesJour = caisseRepository.findByAgentOrderByDateOuvertureDesc(manager);
        long agentsActifs = caissesJour.stream()
                .filter(c -> c.getStatut() == StatutCaisse.OUVERTE)
                .count();

        BigDecimal soldeCaisseTotal = caissesJour.stream()
                .map(c -> c.getSoldeCourant() != null ? c.getSoldeCourant() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Top agents (par volume traité)
        Map<User, BigDecimal> volumeParAgent = new HashMap<>();
        Map<User, BigDecimal> commissionsParAgent = new HashMap<>();
        Map<User, Integer> countParAgent = new HashMap<>();

        for (Transfert t : transfertsJour) {
            if (t.getStatut() != StatutTransfert.PAYE) continue;
            User agent = t.getAgentEnvoi() != null ? t.getAgentEnvoi() : null; // Correction: getAgentEnvoi au lieu de getAgentEmission
            if (agent == null) continue;
            volumeParAgent.merge(agent, t.getMontantEnvoye() != null ? t.getMontantEnvoye() : BigDecimal.ZERO, BigDecimal::add);
            commissionsParAgent.merge(agent, t.getFrais() != null ? t.getFrais() : BigDecimal.ZERO, BigDecimal::add);
            countParAgent.merge(agent, 1, Integer::sum);
        }

        List<ManagerDashboardResponseDTO.AgentStatDTO> topAgents = volumeParAgent.entrySet().stream()
                .sorted(Map.Entry.<User, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    ManagerDashboardResponseDTO.AgentStatDTO stat = new ManagerDashboardResponseDTO.AgentStatDTO();
                    stat.setAgentId(e.getKey().getId());
                    stat.setAgentNom(e.getKey().getNom());
                    stat.setAgentPrenom(e.getKey().getPrenom());
                    stat.setVolumeTraite(e.getValue());
                    stat.setCommissionsGenerees(commissionsParAgent.getOrDefault(e.getKey(), BigDecimal.ZERO));
                    stat.setNombreTransferts(countParAgent.getOrDefault(e.getKey(), 0));
                    return stat;
                })
                .collect(Collectors.toList());

        // Récupérer le pays de l'agence
        Agence agence = manager.getAgence();
        String paysNom = "";
        if (agence.getPays() != null) {
            paysNom = agence.getPays().getNom();
        }

        // Assemblage du DTO
        ManagerDashboardResponseDTO dto = new ManagerDashboardResponseDTO();
        dto.setVolumeJour(volumeJour);
        dto.setCommissionsJour(commissionsJour);
        dto.setTauxSucces(tauxSucces);
        dto.setNombreAgentsActifs((int) agentsActifs);
        dto.setNombreTransfertsJour((int) total);
        dto.setNombreTransfertsEnAttente((int) enAttente);
        dto.setSoldeCaisseTotal(soldeCaisseTotal);
        dto.setTopAgents(topAgents);
        dto.setAgenceNom(agence.getNom());
        dto.setAgencePays(paysNom);
        dto.setAgenceVille(agence.getVille());

        return dto;
    }

    private byte[] buildCsvBytes(ManagerDashboardResponseDTO data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Agence;Volume du jour;Commissions;Taux de succès;Agents actifs;Transferts;En attente;Solde caisse\n");
        sb.append(String.format("%s;%s;%s;%.1f%%;%d;%d;%d;%s\n",
                data.getAgenceNom(),
                data.getVolumeJour(),
                data.getCommissionsJour(),
                data.getTauxSucces(),
                data.getNombreAgentsActifs(),
                data.getNombreTransfertsJour(),
                data.getNombreTransfertsEnAttente(),
                data.getSoldeCaisseTotal()));

        sb.append("\nTop Agents\n");
        sb.append("Nom;Prénom;Transferts;Volume;Commissions\n");
        if (data.getTopAgents() != null) {
            for (var a : data.getTopAgents()) {
                sb.append(String.format("%s;%s;%d;%s;%s\n",
                        a.getAgentNom(),
                        a.getAgentPrenom(),
                        a.getNombreTransferts(),
                        a.getVolumeTraite(),
                        a.getCommissionsGenerees()));
            }
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] buildPdfBytes(ManagerDashboardResponseDTO data) {
        // Pour PDF, on retourne un CSV pour l'instant
        // Implémentez la génération PDF avec iText ou OpenPDF si nécessaire
        String pdfContent = "PDF Report\n" +
                "==========\n" +
                "Agence: " + data.getAgenceNom() + "\n" +
                "Volume du jour: " + data.getVolumeJour() + "\n" +
                "Commissions: " + data.getCommissionsJour() + "\n" +
                "Taux de succès: " + data.getTauxSucces() + "%\n" +
                "Agents actifs: " + data.getNombreAgentsActifs() + "\n" +
                "Transferts: " + data.getNombreTransfertsJour() + "\n" +
                "En attente: " + data.getNombreTransfertsEnAttente() + "\n" +
                "Solde caisse: " + data.getSoldeCaisseTotal();

        return pdfContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private User findManager(String email) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + email));
        if (u.getAgence() == null) {
            throw new BadRequestException("Ce manager n'est affecté à aucune agence");
        }
        return u;
    }
}