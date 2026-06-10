package com.okane.service.impl;

import com.okane.entity.Transfert;
import com.okane.repository.TransfertRepository;
import com.okane.service.RecuPdfService;
import com.okane.util.PdfGenerator;
import static com.okane.util.PdfGenerator.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class RecuPdfServiceImpl implements RecuPdfService {

    @Autowired
    private TransfertRepository transfertRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] genererRecu(Transfert t) throws Exception {
        String ref = "TRF-" + String.format("%06d", t.getId());
        String date = t.getDateCreation() != null ? t.getDateCreation().format(DATE_FMT) : "-";
        String statut = t.getStatut() != null ? labelStatut(t.getStatut().name()) : "N/A";
        String code = t.getCodeRetrait() != null ? t.getCodeRetrait() : "-";

        String expNom = t.getExpediteur() != null
                ? (n(t.getExpediteur().getPrenom()) + " " + n(t.getExpediteur().getNom())).trim()
                : "-";
        String expTel = t.getExpediteur() != null ? n(t.getExpediteur().getTelephone()) : "-";
        String expCin = t.getExpediteur() != null ? n(t.getExpediteur().getNumPieceIdentite()) : "-";

        String benNom = t.getBeneficiaire() != null
                ? (n(t.getBeneficiaire().getPrenom()) + " " + n(t.getBeneficiaire().getNom())).trim()
                : "-";
        String benTel = t.getBeneficiaire() != null ? n(t.getBeneficiaire().getTelephone()) : "-";

        String paysOri = t.getCorridor() != null && t.getCorridor().getPaysOrigine() != null
                ? t.getCorridor().getPaysOrigine().getNom() : "";
        String paysDes = t.getCorridor() != null && t.getCorridor().getPaysDestination() != null
                ? t.getCorridor().getPaysDestination().getNom() : "";
        String corridorDesc = paysOri.isEmpty() && paysDes.isEmpty() ? "-"
                : paysOri + " > " + paysDes;

        String devSrc = t.getCorridor() != null && t.getCorridor().getDeviseSource() != null
                ? t.getCorridor().getDeviseSource().getCode() : "";
        String devDst = t.getCorridor() != null && t.getCorridor().getDeviseDestination() != null
                ? t.getCorridor().getDeviseDestination().getCode() : "";

        BigDecimal fraisTotal = t.getFrais() != null ? t.getFrais() : BigDecimal.ZERO;
        BigDecimal fraisVar = fraisTotal.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fraisFix = fraisTotal.subtract(fraisVar);

        String agentNom = t.getAgentEnvoi() != null
                ? (n(t.getAgentEnvoi().getPrenom()) + " " + n(t.getAgentEnvoi().getNom())).trim()
                : "-";
        String agenceNom = t.getAgenceEnvoi() != null ? n(t.getAgenceEnvoi().getNom()) : "-";

        BigDecimal montantRecu = t.getMontantNet() != null && t.getCorridor() != null && t.getCorridor().getTauxChange() != null
                ? t.getMontantNet().multiply(t.getCorridor().getTauxChange()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        String tauxStr = t.getCorridor() != null && t.getCorridor().getTauxChange() != null
                ? "1 " + devSrc + " = " + fmt(t.getCorridor().getTauxChange()) + " " + devDst
                : "-";
        String receivedStr = fmt(montantRecu) + " " + devDst;

        PdfGenerator pdf = new PdfGenerator();
        pdf.beginPage();

        float y = PAGE_H - MARGIN;
        float col1 = MARGIN + 10;
        float col2 = MARGIN + 280;
        float valX = MARGIN + 380;

        pdf.setColor(0.282f, 0.325f, 0.424f);
        pdf.setFontBold(24);
        pdf.textAt("OKANE", MARGIN, y);
        pdf.setFontRegular(10);
        pdf.setColor(0.435f, 0.502f, 0.569f);
        pdf.textAt("Service de Transfert d'Argent", MARGIN + 88, y - 3);

        y -= 22;
        pdf.setStrokeColor(0.898f, 0.898f, 0.898f);
        pdf.setLineWidth(1);
        pdf.line(MARGIN, y, MARGIN + RW, y);

        y -= 18;
        pdf.setColor(0.231f, 0.529f, 0.800f);
        pdf.setFontBold(16);
        pdf.textCenter("RE\u00c7U DE TRANSFERT", y);
        y -= 19;
        pdf.setFontRegular(8);
        pdf.setColor(0.435f, 0.502f, 0.569f);
        pdf.textCenter("Re\u00e7u officiel \u00e0 conserver par l'exp\u00e9diteur", y);

        y -= 24;
        pdf.setColor(0, 0, 0);
        pdf.setFontRegular(9);
        pdf.textAt("R\u00e9f\u00e9rence", MARGIN, y);
        pdf.setFontBold(9);
        pdf.textAt(ref, MARGIN + 70, y);
        pdf.setFontRegular(9);
        pdf.textAt("Date", MARGIN + 230, y);
        pdf.setFontBold(9);
        pdf.textAt(date, MARGIN + 262, y);
        pdf.setFontRegular(9);
        pdf.textAt("Statut", MARGIN + 410, y);
        pdf.setFontBold(9);
        pdf.setColor(0.071f, 0.659f, 0.275f);
        pdf.textAt(statut, MARGIN + 445, y);

        y -= 30;

        pdf.setColor(0.973f, 0.980f, 0.988f);
        pdf.fillRect(MARGIN, y - 36, RW, 44);
        pdf.setStrokeColor(0.898f, 0.898f, 0.898f);
        pdf.rect(MARGIN, y - 36, RW, 44);

        float codeBoxY = y - 12;
        pdf.setFontSmall(8);
        pdf.setColor(0.435f, 0.502f, 0.569f);
        pdf.textCenter("CODE DE RETRAIT", codeBoxY + 14);
        pdf.setColor(0, 0, 0);
        pdf.setFontBold(22);
        pdf.textCenter(code, codeBoxY - 8);

        y -= 56;

        pdf.setStrokeColor(0.898f, 0.898f, 0.898f);
        pdf.line(MARGIN, y - 2, MARGIN + RW, y - 2);

        y -= 22;
        pdf.setColor(0.231f, 0.529f, 0.800f);
        pdf.setFontBold(10);
        pdf.textAt("EXP\u00c9DITEUR", MARGIN, y);

        y -= 18;
        pdf.setColor(0, 0, 0);
        pdf.setFontRegular(9);
        pdf.textAt("Nom complet", col1, y);
        pdf.setFontBold(9);
        pdf.textAt(expNom, col2, y);
        y -= 15;
        pdf.setFontRegular(9);
        pdf.textAt("T\u00e9l\u00e9phone", col1, y);
        pdf.setFontBold(9);
        pdf.textAt(expTel, col2, y);
        y -= 15;
        pdf.setFontRegular(9);
        pdf.textAt("Pi\u00e8ce d'identit\u00e9", col1, y);
        pdf.setFontBold(9);
        pdf.textAt(expCin, col2, y);

        y -= 26;
        pdf.setColor(0.231f, 0.529f, 0.800f);
        pdf.setFontBold(10);
        pdf.textAt("B\u00c9N\u00c9FICIAIRE", MARGIN, y);

        y -= 18;
        pdf.setColor(0, 0, 0);
        pdf.setFontRegular(9);
        pdf.textAt("Nom complet", col1, y);
        pdf.setFontBold(9);
        pdf.textAt(benNom, col2, y);
        y -= 15;
        pdf.setFontRegular(9);
        pdf.textAt("T\u00e9l\u00e9phone", col1, y);
        pdf.setFontBold(9);
        pdf.textAt(benTel, col2, y);

        y -= 26;
        pdf.setStrokeColor(0.898f, 0.898f, 0.898f);
        pdf.line(MARGIN, y - 2, MARGIN + RW, y - 2);

        y -= 18;
        pdf.setColor(0.231f, 0.529f, 0.800f);
        pdf.setFontBold(10);
        pdf.textAt("D\u00c9TAILS DU TRANSFERT", MARGIN, y);

        y -= 20;
        pdf.setColor(0, 0, 0);
        String[][] rows = {
            {"Corridor", "", corridorDesc},
            {"Montant envoy\u00e9", "", fmt(t.getMontantEnvoye()) + " " + devSrc},
            {"Frais fixes", "", fmt(fraisFix) + " " + devSrc},
            {"Frais proportionnels", "", fmt(fraisVar) + " " + devSrc},
            {"Total des frais", "red", fmt(fraisTotal) + " " + devSrc},
            {"Montant net apr\u00e8s frais", "", fmt(t.getMontantNet()) + " " + devSrc},
            {"Taux de change", "", tauxStr},
        };

        for (int i = 0; i < rows.length; i++) {
            String label = rows[i][0];
            String style = rows[i][1];
            String value = rows[i][2];

            if (label.equals("Total des frais")) {
                y -= 5;
                pdf.setStrokeColor(0.933f, 0.933f, 0.933f);
                pdf.line(col2 - 5, y + 3, col2 - 5 + 200, y + 3);
                y -= 6;
            }

            pdf.setFontRegular(9);
            pdf.setColor(0, 0, 0);
            pdf.textAt(label, col1, y);

            if ("red".equals(style)) {
                pdf.setColor(0.855f, 0.184f, 0.184f);
            }
            pdf.setFontBold(9);
            pdf.textAt(value, valX, y);
            y -= 16;
        }

        pdf.setStrokeColor(0.933f, 0.933f, 0.933f);
        pdf.line(col2 - 5, y + 3, col2 - 5 + 200, y + 3);

        y -= 5;
        pdf.setColor(0.071f, 0.659f, 0.275f);
        pdf.setFontBold(11);
        float labelW = "Montant re\u00e7u par le b\u00e9n\u00e9ficiaire".length() * 5.5f;
        pdf.textAt("Montant re\u00e7u par le b\u00e9n\u00e9ficiaire", MARGIN, y);
        pdf.textAt(receivedStr, valX + 50, y);

        y -= 30;
        pdf.setStrokeColor(0.898f, 0.898f, 0.898f);
        pdf.line(MARGIN, y - 2, MARGIN + RW, y - 2);

        y -= 18;
        pdf.setFontBold(10);
        pdf.setColor(0.435f, 0.502f, 0.569f);
        pdf.textAt("AGENT", MARGIN, y);

        y -= 16;
        pdf.setColor(0, 0, 0);
        pdf.setFontRegular(9);
        pdf.textAt("Agent", col1, y);
        pdf.setFontBold(9);
        pdf.textAt(agentNom, MARGIN + 55, y);
        pdf.setFontRegular(9);
        pdf.textAt("Agence", MARGIN + 250, y);
        pdf.setFontBold(9);
        pdf.textAt(agenceNom, MARGIN + 305, y);

        y = MARGIN + 24;
        pdf.setStrokeColor(0.898f, 0.898f, 0.898f);
        pdf.line(MARGIN, y + 10, MARGIN + RW, y + 10);

        pdf.setFontSmall(7);
        pdf.setColor(0.435f, 0.502f, 0.569f);
        pdf.textCenter("Ce re\u00e7u est g\u00e9n\u00e9r\u00e9 automatiquement par OkaneTransfer. Merci de votre confiance.", y);
        y -= 11;
        pdf.textCenter("OkaneTransfer \u00a9 " + date + " - Tous droits r\u00e9serv\u00e9s", y);

        pdf.endPage();
        return pdf.build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] genererRecuParId(Long transfertId) throws Exception {
        Transfert t = transfertRepository.findById(transfertId)
                .orElseThrow(() -> new RuntimeException("Transfert introuvable: " + transfertId));
        return genererRecu(t);
    }

    private String fmt(BigDecimal d) {
        if (d == null) return "0,00";
        String str = d.setScale(2, RoundingMode.HALF_UP).toPlainString();
        String[] parts = str.split("\\.");
        String intPart = parts[0].replaceAll("(?<=\\d)(?=(\\d{3})+(?!\\d))", "\u00a0");
        return intPart + "," + (parts.length > 1 ? parts[1] : "00");
    }

    private String n(String s) {
        return s != null ? s : "";
    }

    private String labelStatut(String s) {
        switch (s) {
            case "EN_ATTENTE": return "En attente";
            case "PAYE": return "Pay\u00e9";
            case "ANNULE": return "Annul\u00e9";
            case "BLOQUE": return "Bloqu\u00e9";
            default: return s;
        }
    }
}
