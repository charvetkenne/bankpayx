package com.mansa.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper de pagination générique retourné par tous les endpoints d'historique.
 */
@Getter
@Builder
public class PagedResponse<T> {

    private List<T>  content;
    private int      currentPage;
    private int      pageSize;
    private long     totalElements;
    private int      totalPages;
    private boolean  hasNext;
    private boolean  hasPrevious;

    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
