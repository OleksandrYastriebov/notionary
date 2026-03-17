package com.api.wishoria.dto.user;

import com.api.wishoria.dto.wishlist.WishListDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Full public profile of User with user open Wishlists")
public record UserProfileResponseDto(

        @Schema(description = "Basic public information about the requested user",
                requiredMode = Schema.RequiredMode.REQUIRED)
        PublicUserDto user,

        @Schema(description = "List of the user's public wishlists. Private wishlists are strictly excluded from this list",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<WishListDto> publicWishlists
) {
}