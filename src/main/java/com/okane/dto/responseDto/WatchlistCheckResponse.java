package com.okane.dto.responseDto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class WatchlistCheckResponse {
    private boolean matched;
    private String matchedOn;   // "NAME" | "ID_NUMBER" | "BOTH"
    private String matchedEntry;
    private String alertId;
    private String message;
}