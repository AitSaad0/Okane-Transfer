package com.okane.service.impl;

import com.okane.dto.converter.GrilleTarifaireConverter;
import com.okane.dto.requestDto.GrilleTarifaireRequestDTO;
import com.okane.dto.requestDto.SimulationRequestDTO;
import com.okane.dto.responseDto.GrilleTarifaireResponseDTO;
import com.okane.dto.responseDto.SimulationResponseDTO;
import com.okane.entity.Corridor;
import com.okane.entity.GrilleTarifaire;
import com.okane.exception.ResourceNotFoundException;
import com.okane.repository.CorridorRepository;
import com.okane.repository.GrilleTarifaireRepository;
import com.okane.service.GrilleTarifaireService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Color;

@Service
@Transactional
public class GrilleTarifaireServiceImpl implements GrilleTarifaireService {

    @Autowired
    private GrilleTarifaireRepository grilleRepository;

    @Autowired
    private CorridorRepository corridorRepository;

    @Autowired
    private GrilleTarifaireConverter converter;

    @Override
    @Transactional(readOnly = true)
    public List<GrilleTarifaireResponseDTO> findAll() {
        return grilleRepository.findAll().stream()
                .map(converter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GrilleTarifaireResponseDTO findById(Long id) {
        GrilleTarifaire grille = grilleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grille tarifaire not found: " + id));
        return converter.toResponseDTO(grille);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrilleTarifaireResponseDTO> findByCorridorId(Long corridorId) {
        return grilleRepository.findByCorridorId(corridorId).stream()
                .map(converter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GrilleTarifaireResponseDTO save(GrilleTarifaireRequestDTO dto) {
        Corridor corridor = corridorRepository.findById(dto.getCorridorId())
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found: " + dto.getCorridorId()));

        GrilleTarifaire grille = converter.toEntity(dto, corridor);
        GrilleTarifaire saved = grilleRepository.save(grille);
        return converter.toResponseDTO(saved);
    }

    @Override
    public GrilleTarifaireResponseDTO update(Long id, GrilleTarifaireRequestDTO dto) {
        GrilleTarifaire grille = grilleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grille tarifaire not found: " + id));

        converter.updateEntityFromDTO(grille, dto);
        GrilleTarifaire updated = grilleRepository.save(grille);
        return converter.toResponseDTO(updated);
    }

    @Override
    public void delete(Long id) {
        GrilleTarifaire grille = grilleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grille tarifaire not found: " + id));
        grilleRepository.delete(grille);
    }

    @Override
    @Transactional(readOnly = true)
    public SimulationResponseDTO simulate(SimulationRequestDTO dto) {
        Corridor corridor = corridorRepository.findById(dto.getCorridorId())
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found: " + dto.getCorridorId()));

        GrilleTarifaire grille = grilleRepository
                .findByCorridorIdAndMontantMinLessThanEqualAndMontantMaxGreaterThanEqual(
                        dto.getCorridorId(), dto.getMontant(), dto.getMontant())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune grille tarifaire trouvée pour ce montant et ce corridor"));

        return converter.toSimulationDTO(dto.getMontant(), grille, corridor);
    }

    @Override
    public byte[] exportCsv() {
        List<GrilleTarifaire> grilles = grilleRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Corridor,Montant Min,Montant Max,Frais Fixe,Pourcentage,Part Agence,Part Centrale\n");

        for (GrilleTarifaire g : grilles) {
            csv.append(String.format("%d,%s → %s,%s,%s,%s,%.2f%%,%.2f%%,%.2f%%\n",
                    g.getId(),
                    g.getCorridor().getPaysOrigine().getNom(),
                    g.getCorridor().getPaysDestination().getNom(),
                    g.getMontantMin(),
                    g.getMontantMax(),
                    g.getFraisFixe(),
                    g.getPourcentageFrais(),
                    g.getPartAgence(),
                    100.0 - g.getPartAgence()));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportPdf() {
        List<GrilleTarifaire> grilles = grilleRepository.findAll();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // Titre
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("Grilles tarifaires - OkaneTransfer", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Sous-titre
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Paragraph subtitle = new Paragraph("Export généré le " + java.time.LocalDate.now(), subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph(" "));

            // Tableau
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // En-têtes
            String[] headers = {"ID", "Corridor", "Min (MAD)", "Max (MAD)", "Frais Fixe", "% Frais", "Part Agence", "Part Centrale"};
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(79, 70, 229)); // Indigo-600
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Données
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            boolean alternate = false;
            for (GrilleTarifaire g : grilles) {
                Corridor c = g.getCorridor();
                Color bgColor = alternate ? new Color(248, 250, 252) : Color.WHITE; // Slate-50

                String[] values = {
                    g.getId().toString(),
                    c.getPaysOrigine().getNom() + " → " + c.getPaysDestination().getNom(),
                    g.getMontantMin().toString(),
                    g.getMontantMax().toString(),
                    g.getFraisFixe().toString() + " MAD",
                    g.getPourcentageFrais().toString() + "%",
                    g.getPartAgence().toString() + "%",
                    (100.0 - g.getPartAgence()) + "%"
                };

                for (String value : values) {
                    PdfPCell cell = new PdfPCell(new Phrase(value, dataFont));
                    cell.setBackgroundColor(bgColor);
                    cell.setPadding(6);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }
                alternate = !alternate;
            }

            document.add(table);

            // Footer
            document.add(new Paragraph(" "));
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
            Paragraph footer = new Paragraph("© OkaneTransfer - Document confidentiel", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }

        return baos.toByteArray();
    }
}