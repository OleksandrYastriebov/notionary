package com.api.wishoria.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Generic paginated response wrapper")
public record PagedResponse<T>(

        @Schema(description = "Page content")
        List<T> content,

        @Schema(description = "Current page number (0-based)")
        int page,

        @Schema(description = "Number of elements per page")
        int size,

        @Schema(description = "Total number of elements across all pages")
        long totalElements,

        @Schema(description = "Total number of pages")
        int totalPages,

        @Schema(description = "Whether this is the last page")
        boolean last
) {
    public static <T> PagedResponse<T> of(Page<T> springPage) {
        return new PagedResponse<>(
                springPage.getContent(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages(),
                springPage.isLast()
        );
    }
}
