package com.okane.kyc.controller.facade;

import com.okane.kyc.controller.dto.*;
import com.okane.kyc.service.KycService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/kyc")
public class KycController {

    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    /**
     * POST /api/v1/kyc/verify-identity
     * Validates format + checksum, then runs watchlist check.
     */
    @PostMapping("/verify-identity")
    public ResponseEntity<IdentityVerificationResponse> verifyIdentity(
            @Valid @RequestBody IdentityVerificationRequest request) {

        IdentityVerificationResponse response = kycService.verifyIdentity(request);
        // 200 even when invalid — caller reads response.valid
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/kyc/check-watchlist
     * Standalone watchlist check by name and/or id number.
     */
    @PostMapping("/check-watchlist")
    public ResponseEntity<WatchlistCheckResponse> checkWatchlist(
            @Valid @RequestBody WatchlistCheckRequest request) {

        WatchlistCheckResponse response = kycService.checkWatchlist(request);
        return ResponseEntity.ok(response);
    }
}