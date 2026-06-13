package com.okane.controller;

import com.okane.dto.*;
import com.okane.dto.requestDto.WatchlistEntryRequest;
import com.okane.dto.responseDto.WatchlistEntryResponse;
import com.okane.service.KycService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/kyc")
public class AdminKycController {

    private final KycService kycService;

    public AdminKycController(KycService kycService) {
        this.kycService = kycService;
    }
    @GetMapping("/test")
    public String test() {
        return "KYC OK";
    }

    @GetMapping("/watchlist")
    public ResponseEntity<List<WatchlistEntryResponse>> getWatchlist() {
        return ResponseEntity.ok(kycService.getAllWatchlistEntries());
    }

    @PostMapping("/watchlist")
    public ResponseEntity<WatchlistEntryResponse> addToWatchlist(
            @Valid @RequestBody WatchlistEntryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        WatchlistEntryResponse response =
                kycService.addWatchlistEntry(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}