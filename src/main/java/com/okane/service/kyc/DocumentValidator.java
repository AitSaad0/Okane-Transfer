package com.okane.service.kyc;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Validates identity document numbers by country and type.
 * Applies regex format check + optional checksum verification.
 */
public class DocumentValidator {

    public record ValidationResult(boolean valid, String message) {}

    // --- Document format registry ---
    private static final Map<String, DocumentFormat> FORMATS = new HashMap<>();

    static {
        // France CNI: 12 digits
        FORMATS.put("FRA_CNI",      new DocumentFormat(Pattern.compile("^\\d{12}$"), null));
        // France Passport: 2 letters + 7 digits (e.g. AB1234567)
        FORMATS.put("FRA_PASSPORT", new DocumentFormat(Pattern.compile("^[A-Z]{2}\\d{7}$"), null));
        // Maroc CIN: 1-2 letters + 5-7 digits (e.g. AB123456)
        FORMATS.put("MAR_CIN",      new DocumentFormat(Pattern.compile("^[A-Z]{1,2}\\d{5,7}$"), null));
        // Maroc Passport: [A-Z]\d{6} (e.g. A123456)
        FORMATS.put("MAR_PASSPORT", new DocumentFormat(Pattern.compile("^[A-Z]\\d{6}$"), null));
        // Sénégal CNI: 13 digits
        FORMATS.put("SEN_CNI",      new DocumentFormat(Pattern.compile("^\\d{13}$"), null));
        // Côte d'Ivoire CNI: 2 digits + 1 letter + 9 digits (e.g. 12C000000001)
        FORMATS.put("CIV_CNI",      new DocumentFormat(Pattern.compile("^\\d{2}[A-Z]\\d{9}$"), null));
        // Générique ICAO passport (9 chars alphanumeric) avec checksum Luhn-like
        FORMATS.put("GENERIC_PASSPORT", new DocumentFormat(
                Pattern.compile("^[A-Z0-9]{9}$"), DocumentValidator::icaoChecksum));
    }

    private record DocumentFormat(Pattern pattern, ChecksumValidator checksumValidator) {}

    @FunctionalInterface
    private interface ChecksumValidator {
        boolean validate(String number);
    }

    public static ValidationResult validate(String countryCode, String documentType, String number) {
        if (number == null || number.isBlank()) {
            return new ValidationResult(false, "Document number is blank");
        }
        String normalized = number.trim().toUpperCase();
        String key = countryCode.toUpperCase() + "_" + documentType.toUpperCase();

        DocumentFormat format = FORMATS.getOrDefault(key, FORMATS.get("GENERIC_PASSPORT"));
        if (format == null) {
            return new ValidationResult(false, "Unknown document type for country: " + key);
        }

        if (!format.pattern().matcher(normalized).matches()) {
            return new ValidationResult(false,
                    "Document number format invalid for " + key + ": " + normalized);
        }

        if (format.checksumValidator() != null && !format.checksumValidator().validate(normalized)) {
            return new ValidationResult(false, "Checksum verification failed for: " + normalized);
        }

        return new ValidationResult(true, "Document is valid");
    }

    /**
     * ICAO 9303 checksum: each char converted to numeric value,
     * multiplied by weights [7, 3, 1] cyclically, sum mod 10 == 0.
     */
    private static boolean icaoChecksum(String number) {
        int[] weights = {7, 3, 1};
        int sum = 0;
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            int val;
            if (c >= 'A' && c <= 'Z') val = c - 'A' + 10;
            else if (c >= '0' && c <= '9') val = c - '0';
            else return false;
            sum += val * weights[i % 3];
        }
        return sum % 10 == 0;
    }
}