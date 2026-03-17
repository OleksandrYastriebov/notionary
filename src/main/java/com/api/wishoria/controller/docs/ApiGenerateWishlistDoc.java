package com.api.wishoria.controller.docs;

import com.api.wishoria.dto.wishlist.WishListDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Generate a wishlist with items from a text description",
        description = "Uses AI (Google Gemini) to generate a complete wishlist with 3–8 items based on a " +
                "natural language description. The wishlist is saved immediately and returned with all generated items."
)
@ApiResponses({
        @ApiResponse(responseCode = "201", description = "Wishlist successfully generated and saved",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = WishListDto.class)))
})
public @interface ApiGenerateWishlistDoc {
}
