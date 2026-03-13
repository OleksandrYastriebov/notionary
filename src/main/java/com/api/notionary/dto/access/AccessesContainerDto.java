package com.api.notionary.dto.access;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Container holding a wishlists accesses")
public record AccessesContainerDto(
        @Schema(description = "Array of emails")
        List<String> emails
) {
}