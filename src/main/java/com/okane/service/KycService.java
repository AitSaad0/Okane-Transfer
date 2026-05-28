package com.okane.service;

import com.okane.dto.requestDto.IdentityVerificationRequest;
import com.okane.dto.requestDto.WatchlistCheckRequest;
import com.okane.dto.requestDto.WatchlistEntryRequest;
import com.okane.dto.responseDto.IdentityVerificationResponse;
import com.okane.dto.responseDto.WatchlistCheckResponse;
import com.okane.dto.responseDto.WatchlistEntryResponse;
import com.okane.entity.KycAlert;
import com.okane.entity.WatchlistEntry;
import com.okane.repository.KycAlertRepository;
import com.okane.repository.WatchlistEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KycService {

    private final WatchlistEntryRepository watchlistRepo;
    private final KycAlertRepository alertRepo;

    public KycService(WatchlistEntryRepository watchlistRepo,
                      KycAlertRepository alertRepo) {
        this.watchlistRepo = watchlistRepo;
        this.alertRepo     = alertRepo;
    }

    // ------------------------------------------------------------------ //
    //  1. Identity verification
    // ------------------------------------------------------------------ //

    @Transactional
    public IdentityVerificationResponse verifyIdentity(IdentityVerificationRequest req) {

        // Step 1: format + checksum check
        DocumentValidator.ValidationResult docResult =
                DocumentValidator.validate(req.getCountryCode(),
                        req.getDocumentType(),
                        req.getDocumentNumber());

        if (!docResult.valid()) {
            // Generate LOW alert for bad document format
            KycAlert alert = createAlert("INVALID_DOCUMENT", "LOW",
                    "Invalid document: " + docResult.message(),
                    req.getFullName(), req.getDocumentNumber());

            return IdentityVerificationResponse.builder()
                    .valid(false)
                    .documentNumber(req.getDocumentNumber())
                    .countryCode(req.getCountryCode())
                    .documentType(req.getDocumentType())
                    .message(docResult.message())
                    .watchlistMatch(false)
                    .alertId(alert.getId().toString())
                    .build();
        }

        // Step 2: watchlist check
        WatchlistCheckResponse watchCheck = checkWatchlist(
                new WatchlistCheckRequest() {{
                    setFullName(req.getFullName());
                    setIdNumber(req.getDocumentNumber());
                }});

        if (watchCheck.isMatched()) {
            return IdentityVerificationResponse.builder()
                    .valid(false)
                    .documentNumber(req.getDocumentNumber())
                    .countryCode(req.getCountryCode())
                    .documentType(req.getDocumentType())
                    .message("BLOCKED: subject matches OFAC watchlist on " + watchCheck.getMatchedOn())
                    .watchlistMatch(true)
                    .alertId(watchCheck.getAlertId())
                    .build();
        }

        return IdentityVerificationResponse.builder()
                .valid(true)
                .documentNumber(req.getDocumentNumber())
                .countryCode(req.getCountryCode())
                .documentType(req.getDocumentType())
                .message("Identity verified successfully")
                .watchlistMatch(false)
                .build();
    }

    // ------------------------------------------------------------------ //
    //  2. Watchlist check
    // ------------------------------------------------------------------ //

    @Transactional
    public WatchlistCheckResponse checkWatchlist(WatchlistCheckRequest req) {

        boolean nameMatch = false;
        boolean idMatch   = false;
        WatchlistEntry matchedEntry = null;

        // ID number exact match
        if (req.getIdNumber() != null && !req.getIdNumber().isBlank()) {
            Optional<WatchlistEntry> byId =
                    watchlistRepo.findByIdNumber(req.getIdNumber().trim().toUpperCase());
            if (byId.isPresent()) {
                idMatch      = true;
                matchedEntry = byId.get();
            }
        }

        // Name fuzzy match (token-based: every word of searched name must appear in entry name)
        if (!idMatch) {
            List<WatchlistEntry> byName =
                    watchlistRepo.findByNameContaining(req.getFullName());
            if (!byName.isEmpty()) {
                nameMatch    = true;
                matchedEntry = byName.get(0);
            }
        }

        if (!nameMatch && !idMatch) {
            return WatchlistCheckResponse.builder()
                    .matched(false)
                    .message("No match found in watchlist")
                    .build();
        }

        String matchedOn = (nameMatch && idMatch) ? "BOTH" : (idMatch ? "ID_NUMBER" : "NAME");

        // Generate HIGH alert
        KycAlert alert = createAlert("WATCHLIST_MATCH", "HIGH",
                "OFAC watchlist match on " + matchedOn + " for: " + req.getFullName(),
                req.getFullName(), req.getIdNumber());

        return WatchlistCheckResponse.builder()
                .matched(true)
                .matchedOn(matchedOn)
                .matchedEntry(matchedEntry.getFullName() + " [" + matchedEntry.getSource() + "]")
                .alertId(alert.getId().toString())
                .message("BLOCKED: subject is on OFAC watchlist")
                .build();
    }

    // ------------------------------------------------------------------ //
    //  3. Watchlist CRUD (admin)
    // ------------------------------------------------------------------ //

    @Transactional(readOnly = true)
    public List<WatchlistEntryResponse> getAllWatchlistEntries() {
        return watchlistRepo.findAll().stream()
                .map(this::toWatchlistResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WatchlistEntryResponse addWatchlistEntry(WatchlistEntryRequest req, String adminEmail) {
        WatchlistEntry entry = WatchlistEntry.builder()
                .fullName(req.getFullName())
                .idNumber(req.getIdNumber() != null
                        ? req.getIdNumber().trim().toUpperCase() : null)
                .source(req.getSource())
                .reason(req.getReason())
                .addedAt(LocalDateTime.now())
                .addedBy(adminEmail)
                .build();
        watchlistRepo.save(entry);
        return toWatchlistResponse(entry);
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private KycAlert createAlert(String type, String severity,
                                 String message, String name, String idNumber) {
        KycAlert alert = KycAlert.builder()
                .alertType(type)
                .severity(severity)
                .message(message)
                .subjectName(name)
                .subjectIdNumber(idNumber)
                .createdAt(LocalDateTime.now())
                .status("OPEN")
                .build();
        return alertRepo.save(alert);
    }

    private WatchlistEntryResponse toWatchlistResponse(WatchlistEntry e) {
        return WatchlistEntryResponse.builder()
                .id(e.getId())
                .fullName(e.getFullName())
                .idNumber(e.getIdNumber())
                .source(e.getSource())
                .reason(e.getReason())
                .addedAt(e.getAddedAt())
                .addedBy(e.getAddedBy())
                .build();
    }
}