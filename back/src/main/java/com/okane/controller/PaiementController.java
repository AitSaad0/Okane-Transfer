package com.okane.controller;

import com.okane.dto.requestDto.PaiementRequestDTO;
import com.okane.dto.responseDto.PaiementResponseDTO;
import com.okane.service.PaiementService;
import com.okane.service.RecuPdfService;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent/transfers")
public class PaiementController {

    private static final Logger log = LoggerFactory.getLogger(PaiementController.class);

    @Autowired
    private PaiementService paiementService;

    @Autowired
    @Qualifier("recuPaiementPdfServiceImpl")
    private RecuPdfService recuPaiementPdfService;

    @GetMapping("/search/code")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<PaiementResponseDTO> rechercherParCodeRetrait(
            @RequestParam("codeRetrait") String codeRetrait) {
        PaiementResponseDTO response = paiementService.rechercherParCodeRetrait(codeRetrait);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/telephone")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<List<PaiementResponseDTO>> rechercherParTelephoneBeneficiaire(
            @RequestParam("telephone") String telephone) {
        List<PaiementResponseDTO> response = paiementService.rechercherParTelephoneBeneficiaire(telephone);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/payer")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<PaiementResponseDTO> payerTransfert(
            @PathVariable Long id,
            @Valid @RequestBody PaiementRequestDTO request,
            Authentication authentication) {
        request.setTransfertId(id);
        PaiementResponseDTO response = paiementService.payerTransfert(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/recu-paiement")
    @PreAuthorize("hasRole('AGENT')")
    public void telechargerRecuPaiement(@PathVariable("id") Long id, HttpServletResponse response) {
        log.info("PDF payment receipt request for transfer id={}", id);
        try {
            byte[] pdf = recuPaiementPdfService.genererRecuParId(id);
            log.info("PDF payment receipt generated, size={} bytes", pdf.length);

            response.setContentType("application/pdf");
            response.setContentLength(pdf.length);
            response.getOutputStream().write(pdf);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("PDF payment receipt generation failed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
