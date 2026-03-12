package com.api.notionary.dto.payload.request.wishlistitem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload to toggle the completion status of a wishlist item")
public record WishlistItemIsCheckedRequest(
        @Schema(description = "True if the item is fulfilled/purchased, false otherwise",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "isChecked flag cannot be null")
        Boolean isChecked
) {
}