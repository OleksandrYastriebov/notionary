package com.api.wishoria.dto.user;

import com.api.wishoria.dto.PagedResponse;
import com.api.wishoria.dto.wishlist.WishListDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Full public profile of User with user open Wishlists")
public record UserProfileResponseDto(

        @Schema(description = "Basic public information about the requested user",
                requiredMode = Schema.RequiredMode.REQUIRED)
        PublicUserDto user,

        @Schema(description = "Paginated list of wishlists available to the viewer",
                requiredMode = Schema.RequiredMode.REQUIRED)
        PagedResponse<WishListDto> publicWishlists
) {
}