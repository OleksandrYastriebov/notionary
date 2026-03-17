package com.api.wishoria.controller;

import com.api.wishoria.controller.docs.ApiForbiddenErrorDoc;
import com.api.wishoria.controller.docs.ApiNotFoundErrorDoc;
import com.api.wishoria.controller.docs.ApiUnauthorizedErrorDoc;
import com.api.wishoria.dto.ApiResponseWrapper;
import com.api.wishoria.dto.access.AccessesContainerDto;
import com.api.wishoria.dto.payload.request.wishlist.RevokeAccessRequest;
import com.api.wishoria.dto.payload.request.wishlist.ShareWishListRequest;
import com.api.wishoria.entity.User;
import com.api.wishoria.security.interceptor.RateLimitPlan;
import com.api.wishoria.security.interceptor.RateLimited;
import com.api.wishoria.service.WishListAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Wishlist Access", description = "Endpoints for managing access to private wishlists")
@RateLimited(action = RateLimitPlan.MUTATION)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/wishlists/{wishlistId}/access")
public class WishListAccessController {

    private final WishListAccessService wishlistAccessService;

    @Operation(summary = "Get all granted accesses",
            description = "Returns a list of emails of users who have been granted access to this private wishlist. Only the owner can view this list.")
    @ApiUnauthorizedErrorDoc
    @ApiNotFoundErrorDoc
    @ApiForbiddenErrorDoc
    @GetMapping
    public ResponseEntity<AccessesContainerDto> getAllGrantedAccesses(@Parameter(description = "ID of the wishlist") @PathVariable
                                                                      String wishlistId,
                                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(wishlistAccessService.getAllGrantedEmailsForWishlist(wishlistId, user));
    }

    @Operation(summary = "Grant access", description = "Allows the owner to share a private wishlist with another user via email.")
    @ApiUnauthorizedErrorDoc
    @ApiNotFoundErrorDoc
    @ApiForbiddenErrorDoc
    @PostMapping
    public ResponseEntity<ApiResponseWrapper> grantAccess(@PathVariable String wishlistId,
                                                          @Valid @RequestBody ShareWishListRequest request,
                                                          @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(wishlistAccessService.grantAccess(wishlistId, request, user));
    }

    @Operation(summary = "Revoke access", description = "Allows the owner to remove a user's access to their private wishlist.")
    @ApiUnauthorizedErrorDoc
    @ApiNotFoundErrorDoc
    @ApiForbiddenErrorDoc
    @DeleteMapping
    public ResponseEntity<ApiResponseWrapper> revokeAccess(@PathVariable String wishlistId,
                                                           @Valid @RequestBody RevokeAccessRequest revokeAccessRequest,
                                                           @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(wishlistAccessService.revokeAccess(wishlistId, revokeAccessRequest, user));
    }
}