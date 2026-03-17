package com.api.notionary.service.ai;

import com.api.notionary.dto.ai.AiWishlistGenerationDto;
import com.api.notionary.dto.payload.request.ai.GenerateDescriptionRequest;
import com.api.notionary.dto.payload.request.ai.GenerateWishlistRequest;
import com.api.notionary.dto.payload.request.wishlist.CreateWishlistRequest;
import com.api.notionary.dto.payload.request.wishlistitem.CreateWishListItemRequest;
import com.api.notionary.dto.wishlist.WishListDto;
import com.api.notionary.entity.User;
import com.api.notionary.service.WishListItemService;
import com.api.notionary.service.WishListService;
import com.api.notionary.service.ai.util.AiPrompts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;

import java.util.Base64;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AiAssistantService {

    private static final int MAX_AI_ITEMS = 10;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    private final ChatClient chatClient;
    private final WishListService wishListService;
    private final WishListItemService wishListItemService;

    public String generateDescription(GenerateDescriptionRequest request, String wishlistId, User currentUser) {
        wishListService.getWishlistEntityForOwner(wishlistId, currentUser);
        String title = truncate(request.title(), MAX_TITLE_LENGTH);
        try {
            if (hasImage(request.base64Image())) {
                return generateMultimodalDescription(title, request.base64Image(), request.mimeType());
            }
            return chatClient.prompt()
                    .user(AiPrompts.descriptionPrompt(title))
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("Error communicating with AI Assistant via Spring AI", ex);
            throw new RuntimeException("Impossible to generate description. Please try again later.");
        }
    }

    @Transactional
    public WishListDto generateWishlist(GenerateWishlistRequest request, User currentUser) {
        AiWishlistGenerationDto generated = fetchWishlistFromAi(request.description());

        WishListDto wishList = wishListService.createWishlist(
                new CreateWishlistRequest(generated.title(), request.isPublic(), null),
                currentUser
        );

        saveItems(wishList.id(), generated.items(), currentUser);

        return wishListService.findWishlistById(wishList.id(), currentUser);
    }

    private AiWishlistGenerationDto fetchWishlistFromAi(String description) {
        try {
            AiWishlistGenerationDto response = chatClient.prompt()
                    .user(AiPrompts.wishlistGenerationPrompt(description))
                    .call()
                    .entity(AiWishlistGenerationDto.class);
            validateResponse(response);
            return response;
        } catch (RuntimeException ex) {
            log.error("AI wishlist generation failed", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during AI wishlist generation", ex);
            throw new RuntimeException("Failed to generate wishlist. Please try again later.");
        }
    }

    private void validateResponse(AiWishlistGenerationDto response) {
        if (response == null) {
            throw new RuntimeException("AI returned an empty response.");
        }
        if (response.title() == null || response.title().isBlank()) {
            throw new RuntimeException("AI returned a wishlist without a title.");
        }
        if (response.items() == null || response.items().isEmpty()) {
            throw new RuntimeException("AI returned a wishlist without items.");
        }
    }

    private void saveItems(String wishlistId, List<AiWishlistGenerationDto.AiWishlistItemDto> items, User currentUser) {
        items.stream()
                .limit(MAX_AI_ITEMS)
                .filter(item -> item.title() != null && !item.title().isBlank())
                .forEach(item -> wishListItemService.createWishListItem(
                        wishlistId,
                        new CreateWishListItemRequest(
                                truncate(item.title(), MAX_TITLE_LENGTH),
                                item.url(),
                                item.price(),
                                truncate(item.description(), MAX_DESCRIPTION_LENGTH),
                                null
                        ),
                        currentUser
                ));
    }

    private boolean hasImage(String base64Image) {
        return base64Image != null && !base64Image.isBlank();
    }

    private String generateMultimodalDescription(String title, String base64Image, String mimeType) {
        String cleanBase64 = base64Image.replaceFirst("^data:image/[^;]+;base64,", "");
        byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes);

        return chatClient.prompt()
                .user(user -> user.text(AiPrompts.descriptionPrompt(title))
                        .media(MimeTypeUtils.parseMimeType(mimeType), imageResource))
                .call()
                .content();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
