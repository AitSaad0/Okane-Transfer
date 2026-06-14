package com.okane.dto.responseDto;

import com.okane.entity.JournalAudit;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class JournalAuditResponseDTO {

    private Long          id;
    private String        action;
    private String        details;
    private String        type;
    private LocalDateTime timestamp;
    private String        ipAddress;

    // flat — avoids triggering lazy-load outside a transaction
    private Long          utilisateurId;
    private String        utilisateurEmail;
    private String        utilisateurNom;     // useful for admin display

    private Long          transfertId;        // nullable — not every action has a transfert

    // ------------------------------------------------------------------ //
    //  Static factory — keeps the controller/service clean
    // ------------------------------------------------------------------ //

    public static JournalAuditResponseDTO from(JournalAudit j) {
        return JournalAuditResponseDTO.builder()
                .id(j.getId())
                .action(j.getAction())
                .details(j.getDetails())
                .type(j.getType())
                .timestamp(j.getTimestamp())
                .ipAddress(j.getIpAddress())
                .utilisateurId(j.getUtilisateur().getId())
                .utilisateurEmail(j.getUtilisateur().getEmail())
                .utilisateurNom(j.getUtilisateur().getNom())
                .transfertId(j.getTransfert() != null ? j.getTransfert().getId() : null)
                .build();
    }
}