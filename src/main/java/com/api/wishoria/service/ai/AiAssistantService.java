package com.api.wishoria.service.ai;

import com.api.wishoria.dto.ai.AiWishlistGenerationDto;
import com.api.wishoria.dto.ai.GiftSuggestionsDto;
import com.api.wishoria.dto.ai.request.GenerateDescriptionRequest;
import com.api.wishoria.dto.ai.request.GenerateWishlistRequest;
import com.api.wishoria.dto.wishlist.request.CreateWishlistRequest;
import com.api.wishoria.dto.wishlistitem.request.CreateWishListItemRequest;
import com.api.wishoria.dto.wishlist.WishListDto;
import com.api.wishoria.entity.User;
import com.api.wishoria.service.UserService;
import com.api.wishoria.service.WishListItemService;
import com.api.wishoria.service.WishListService;
import com.api.wishoria.service.ai.util.AiPrompts;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

import static com.api.wishoria.service.ai.util.AiResponseSanitizationUtil.sanitizeUrl;
import static com.api.wishoria.service.ai.util.AiResponseSanitizationUtil.parsePriceSafe;
import static com.api.wishoria.service.ai.util.AiResponseSanitizationUtil.truncate;

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
    private final UserService userService;

    public String generateDescription(GenerateDescriptionRequest request, String wishlistId, User currentUser) {
        wishListService.getWishlistEntityForOwner(wishlistId, currentUser);
        String title = truncate(request.title(), MAX_TITLE_LENGTH);
        try {
            if (StringUtils.hasText(request.base64Image())) {
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
                new CreateWishlistRequest(truncate(generated.title(), MAX_TITLE_LENGTH),
                        request.isPublic() != null ? request.isPublic() : false,
                        null),
                currentUser
        );

        saveItems(wishList.id(), generated.items(), currentUser);

        return wishListService.findWishlistById(wishList.id(), currentUser);
    }

    private AiWishlistGenerationDto fetchWishlistFromAi(String description) {
        try {
            AiWishlistGenerationDto response = chatClient.prompt()
                    .system(AiPrompts.wishlistGenerationSystemPrompt())
                    .user(description)
                    .call()
                    .entity(AiWishlistGenerationDto.class);
            validateResponse(response);
            return response;
        } catch (Exception ex) {
            log.error("Failed to generate wishlist via AI. Input: {}", description, ex);
            throw new RuntimeException("Failed to generate wishlist. Please try again later.");
        }
    }

    private void validateResponse(AiWishlistGenerationDto response) {
        if (response == null) {
            throw new RuntimeException("AI returned an empty response.");
        }
        if (!StringUtils.hasText(response.title())) {
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
                .forEach(item -> {
                    BigDecimal parsedPrice = parsePriceSafe(item.price());
                    String safeUrl = sanitizeUrl(item.url());

                    wishListItemService.createWishListItem(
                            wishlistId,
                            new CreateWishListItemRequest(
                                    truncate(item.title(), MAX_TITLE_LENGTH),
                                    safeUrl,
                                    parsedPrice,
                                    truncate(item.description(), MAX_DESCRIPTION_LENGTH),
                                    null
                            ),
                            currentUser
                    );
                });
    }

    public GiftSuggestionsDto generateGiftSuggestions(Long userId, User currentUser) {
        User targetUser = userService.getUserById(userId);
        List<WishListDto> wishlists = wishListService.getAvailableWishlists(userId, currentUser, 0, 50).content();

        String wishlistsSummary = buildWishlistsSummary(wishlists);
        String prompt = AiPrompts.giftSuggestionsPrompt(targetUser.getProfileDescription(), wishlistsSummary);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            List<String> suggestions = parseSuggestions(response);
            return new GiftSuggestionsDto(suggestions);
        } catch (Exception ex) {
            log.error("Error generating gift suggestions for userId={}", userId, ex);
            throw new RuntimeException("Failed to generate gift suggestions. Please try again later.");
        }
    }

    private String buildWishlistsSummary(List<WishListDto> wishlists) {
        if (wishlists.isEmpty()) {
            return "No public wishlists available.";
        }
        StringBuilder sb = new StringBuilder();
        for (WishListDto wl : wishlists) {
            sb.append("- Wishlist: ").append(wl.title()).append("\n");
            if (wl.wishListItems() != null) {
                wl.wishListItems().forEach(item ->
                        sb.append("  * ").append(item.title()).append("\n"));
            }
        }
        return sb.toString();
    }

    private List<String> parseSuggestions(String response) {
        try {
            String clean = response.replaceAll("```json|```", "").trim();
            ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(clean, new TypeReference<>() {
            });
        } catch (Exception ex) {
            log.warn("Could not parse AI gift suggestions as JSON array, returning raw response");
            return List.of(response);
        }
    }

    private String generateMultimodalDescription(String title, String base64Image, String mimeType) {
        String cleanBase64 = satitizeBase64String(base64Image);
        byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes);

        return chatClient.prompt()
                .user(user -> user.text(AiPrompts.descriptionPrompt(title))
                        .media(MimeTypeUtils.parseMimeType(mimeType), imageResource))
                .call()
                .content();
    }

    private String satitizeBase64String(String base64Image) {
        return base64Image
                .replaceFirst("^data:image/[^;]+;base64,", "")
                .replaceAll("\\s+", "");
    }

}
