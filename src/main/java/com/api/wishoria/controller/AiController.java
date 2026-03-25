package com.api.wishoria.controller;

import com.api.wishoria.controller.docs.ApiBadRequestDoc;
import com.api.wishoria.controller.docs.ApiGenerateWishlistDoc;
import com.api.wishoria.controller.docs.ApiUnauthorizedErrorDoc;
import com.api.wishoria.controller.docs.ApiWishlistLimitReachedDoc;
import com.api.wishoria.dto.ai.AiDescriptionDto;
import com.api.wishoria.dto.ai.GiftSuggestionsDto;
import com.api.wishoria.dto.ai.request.GenerateDescriptionRequest;
import com.api.wishoria.dto.ai.request.GenerateWishlistRequest;
import com.api.wishoria.dto.wishlist.WishListDto;
import com.api.wishoria.entity.User;
import com.api.wishoria.security.interceptor.RateLimitPlan;
import com.api.wishoria.security.interceptor.RateLimited;
import com.api.wishoria.service.ai.AiAssistantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI Assistant", description = "API for interacting with Artificial Intelligence (content generation)")
@RateLimited(action = RateLimitPlan.DEFAULT)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/wishlists/{wishlistId}/generate-description")
    public ResponseEntity<AiDescriptionDto> generateDescription(@Valid @RequestBody GenerateDescriptionRequest request,
                                                                @PathVariable String wishlistId,
                                                                @AuthenticationPrincipal User user) {
        String description = aiAssistantService.generateDescription(request, wishlistId, user);
        return ResponseEntity.ok(new AiDescriptionDto(description));
    }

    @ApiGenerateWishlistDoc
    @ApiBadRequestDoc
    @ApiUnauthorizedErrorDoc
    @ApiWishlistLimitReachedDoc
    @PostMapping("/wishlists/generate-wishlists")
    public ResponseEntity<WishListDto> generateWishlist(@Valid @RequestBody GenerateWishlistRequest request,
                                                        @AuthenticationPrincipal User user) {
        WishListDto wishList = aiAssistantService.generateWishlist(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(wishList);
    }

    @PostMapping("/users/{userId}/gift-suggestions")
    public ResponseEntity<GiftSuggestionsDto> generateGiftSuggestions(@PathVariable Long userId,
                                                                       @AuthenticationPrincipal User user) {
        GiftSuggestionsDto suggestions = aiAssistantService.generateGiftSuggestions(userId, user);
        return ResponseEntity.ok(suggestions);
    }
}
