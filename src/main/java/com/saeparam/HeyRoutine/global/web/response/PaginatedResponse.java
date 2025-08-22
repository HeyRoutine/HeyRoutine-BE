package com.saeparam.HeyRoutine.global.web.response;

import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generic wrapper for paginated API responses.
 *
 * @param page       current page index (0-based)
 * @param pageSize   number of items per page
 * @param totalItems total number of items across all pages
 * @param totalPages total number of pages
 * @param items      list of items for the current page
 */
@Builder
public record PaginatedResponse<T>(
        int page,
        int pageSize,
        long totalItems,
        int totalPages,
        List<T> items
) {
    public static <T> PaginatedResponse<T> of(Page<T> page) {
        return PaginatedResponse.<T>builder()
                .page(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(page.getContent())
                .build();
    }

    public static <E, T> PaginatedResponse<T> of(Page<E> page, Function<? super E, ? extends T> mapper) {
        List<T> mapped = page.getContent()
                .stream()
                .map(mapper)
                .collect(Collectors.toList());

        return PaginatedResponse.<T>builder()
                .page(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(mapped)
                .build();
    }
}

