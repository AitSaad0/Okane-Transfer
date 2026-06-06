package com.okane.dto.responseDto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic pagination wrapper.
 *
 * Usage in service/controller:
 *   PageResponseDTO<JournalAuditResponseDTO> response =
 *       PageResponseDTO.from(journalAuditService.findAll(pageable));
 *
 * Why not return Spring's Page<> directly?
 *   Spring's Page serialises fine, but it includes internal fields the
 *   front-end doesn't need. This wrapper keeps the contract clean and
 *   stable even if you swap the persistence layer later.
 */
@Getter
@Builder
public class PageResponseDTO<T> {

    private List<T> content;

    private int  page;           // current 0-based page index
    private int  size;           // page size requested
    private long totalElements;  // total rows in the table
    private int  totalPages;     // derived: ceil(totalElements / size)
    private boolean first;       // is this the first page?
    private boolean last;        // is this the last page?

    // ------------------------------------------------------------------ //
    //  Static factory
    // ------------------------------------------------------------------ //

    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return PageResponseDTO.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}