package com.api.wishoria.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Internal DTO used only for parsing the structured JSON response from the AI model.
 * Not exposed directly via API — converted to WishListDto after saving.
 */
public record AiWishlistGenerationDto(

        @JsonProperty(required = true, value = "title")
        String title,

        @JsonProperty(required = true, value = "items")
        List<AiWishlistItemDto> items
) {
    public record AiWishlistItemDto(

            @JsonProperty(required = true, value = "title")
            String title,

            @JsonProperty(value = "description")
            String description,

            @JsonProperty(value = "price")
            String price,

            @JsonProperty(value = "url")
            String url
    ) {}
}
