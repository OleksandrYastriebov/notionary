package com.api.wishoria.dto.wishlist;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Container holding a list of wishlists")
public record WishListContainerDto(
        @Schema(description = "Array of wishlists")
        List<WishListDto> wishLists
) {
}