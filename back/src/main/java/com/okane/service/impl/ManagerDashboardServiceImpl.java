package com.okane.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
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

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
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
        // Toutes les caisses ouvertes de l'agence aujourd'hui
        List<Caisse> caissesJour =
                caisseRepository.findByAgenceIdAndDateAndStatut(
                        agenceId,
                        date,
                        StatutCaisse.OUVERTE
                );


        for (Caisse c : caissesJour) {
            System.out.println(
                    "Caisse ID=" + c.getId()
                            + " solde=" + c.getSoldeCourant()
                            + " statut=" + c.getStatut()
                            + " agence=" + c.getAgence().getId()
            );
        }

        long agentsActifs = caissesJour.size();

        BigDecimal soldeCaisseTotal = caissesJour.stream()
                .map(c -> c.getSoldeCourant() != null
                        ? c.getSoldeCourant()
                        : BigDecimal.ZERO)
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

        sb.append('\uFEFF');
        sb.append("sep=;\r\n");

        // Header row
        sb.append("Agence;Ville;Pays;Volume du jour;Commissions;Taux de succes (%);Agents actifs;Nombre transferts;En attente;Solde caisse\r\n");

        // Values row
        sb.append(csv(data.getAgenceNom())).append(";")
                .append(csv(data.getAgenceVille())).append(";")
                .append(csv(data.getAgencePays())).append(";")
                .append(csv(data.getVolumeJour())).append(";")
                .append(csv(data.getCommissionsJour())).append(";")
                .append(data.getTauxSucces()).append(";")
                .append(data.getNombreAgentsActifs()).append(";")
                .append(data.getNombreTransfertsJour()).append(";")
                .append(data.getNombreTransfertsEnAttente()).append(";")
                .append(csv(data.getSoldeCaisseTotal()))
                .append("\r\n");

        sb.append("\r\n");

        // Top agents table
        sb.append("Nom;Prenom;Nombre transferts;Volume traite;Commissions generees\r\n");

        if (data.getTopAgents() != null) {
            for (ManagerDashboardResponseDTO.AgentStatDTO agent : data.getTopAgents()) {
                sb.append(csv(agent.getAgentNom())).append(";")
                        .append(csv(agent.getAgentPrenom())).append(";")
                        .append(agent.getNombreTransferts()).append(";")
                        .append(csv(agent.getVolumeTraite())).append(";")
                        .append(csv(agent.getCommissionsGenerees()))
                        .append("\r\n");
            }
        }

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    private String csv(Object value) {

        if (value == null) {
            return "";
        }

        String text = value.toString();

        text = text.replace("\"", "\"\"");

        return "\"" + text + "\"";
    }

    private byte[] buildPdfBytes(ManagerDashboardResponseDTO data) {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 12);

            document.add(new Paragraph("Rapport Journalier - OKANE", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Agence : " + data.getAgenceNom(), normalFont));
            document.add(new Paragraph("Ville : " + data.getAgenceVille(), normalFont));
            document.add(new Paragraph("Pays : " + data.getAgencePays(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Indicateurs", sectionFont));
            document.add(new Paragraph("Volume du jour : " + data.getVolumeJour(), normalFont));
            document.add(new Paragraph("Commissions : " + data.getCommissionsJour(), normalFont));
            document.add(new Paragraph("Taux de succès : " + data.getTauxSucces() + "%", normalFont));
            document.add(new Paragraph("Agents actifs : " + data.getNombreAgentsActifs(), normalFont));
            document.add(new Paragraph("Nombre de transferts : " + data.getNombreTransfertsJour(), normalFont));
            document.add(new Paragraph("Transferts en attente : " + data.getNombreTransfertsEnAttente(), normalFont));
            document.add(new Paragraph("Solde caisse total : " + data.getSoldeCaisseTotal(), normalFont));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Top Agents", sectionFont));

            if (data.getTopAgents() != null) {

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);

                table.addCell("Nom");
                table.addCell("Prénom");
                table.addCell("Transferts");
                table.addCell("Volume");
                table.addCell("Commissions");

                for (ManagerDashboardResponseDTO.AgentStatDTO agent : data.getTopAgents()) {

                    table.addCell(agent.getAgentNom());
                    table.addCell(agent.getAgentPrenom());
                    table.addCell(String.valueOf(agent.getNombreTransferts()));
                    table.addCell(String.valueOf(agent.getVolumeTraite()));
                    table.addCell(String.valueOf(agent.getCommissionsGenerees()));
                }

                document.add(table);
            }

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
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