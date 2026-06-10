package com.okane.controller;

import com.okane.service.RecuPdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent/transfers")
public class RecuPdfController {

    private static final Logger log = LoggerFactory.getLogger(RecuPdfController.class);

    @Autowired
    private RecuPdfService recuPdfService;

    @GetMapping("/{id}/recu")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<byte[]> telechargerRecu(@PathVariable("id") Long id) {
        log.info("PDF request for transfer id={}", id);

        try {
            byte[] pdf = recuPdfService.genererRecuParId(id);
            log.info("PDF generated, size={} bytes", pdf.length);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"recu-transfert-" + id + ".pdf\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);
        } catch (Exception e) {
            log.error("PDF generation failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
