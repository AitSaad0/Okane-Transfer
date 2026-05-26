package com.okane.controller.kyc;

import com.okane.dto.kyc.*;
import com.okane.service.kyc.KycService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/kyc")
public class AdminKycController {

    private final KycService kycService;

    public AdminKycController(KycService kycService) {
        this.kycService = kycService;
    }

    @GetMapping("/watchlist")
    public ResponseEntity<List<WatchlistEntryResponse>> getWatchlist() {
        return ResponseEntity.ok(kycService.getAllWatchlistEntries());
    }

    @PostMapping("/watchlist")
    public ResponseEntity<WatchlistEntryResponse> addToWatchlist(
            @Valid @RequestBody WatchlistEntryRequest request) {

        WatchlistEntryResponse response =
                kycService.addWatchlistEntry(request, "test-admin@okane.com");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}