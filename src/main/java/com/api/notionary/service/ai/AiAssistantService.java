package com.api.notionary.service.ai;

import com.api.notionary.dto.payload.request.ai.GenerateDescriptionRequest;
import com.api.notionary.entity.User;
import com.api.notionary.service.WishListService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.util.Base64;

@Service
@Slf4j
public class AiAssistantService {

    private final ChatClient chatClient;
    private final WishListService wishListService;

    public AiAssistantService(ChatClient.Builder builder, WishListService wishListService) {
        this.chatClient = builder.build();
        this.wishListService = wishListService;
    }

    public String generateDescription(GenerateDescriptionRequest descriptionRequest,
                                      String wishlistId, User currentuUser) {
        wishListService.getWishlistEntityForOwner(wishlistId, currentuUser);

        String promptText = getDescriptionPrompt(descriptionRequest.title());
        String base64Image = descriptionRequest.base64Image();
        try {
            if (hasImage(base64Image)) {
                return generateMultimodalResponse(promptText, base64Image, descriptionRequest.mimeType());
            }
            return generateTextResponse(promptText);

        } catch (Exception ex) {
            log.error("Error communicating with AI Assistant via Spring AI", ex);
            throw new RuntimeException("Impossible to generate description. Please try again later.");
        }
    }

    private boolean hasImage(String base64Image) {
        return base64Image != null && !base64Image.isBlank();
    }

    private String generateMultimodalResponse(String promptText, String base64Image, String mimeType) {
        String cleanBase64 = base64Image.replaceFirst("^data:image/[^;]+;base64,", "");
        byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes);

        return chatClient.prompt()
                .user(user -> user.text(promptText)
                        .media(MimeTypeUtils.parseMimeType(mimeType), imageResource))
                .call()
                .content();
    }

    private String generateTextResponse(String promptText) {
        return chatClient.prompt()
                .user(promptText)
                .call()
                .content();
    }

    private @NonNull String getDescriptionPrompt(String title) {
        return String.format("You are creative UX-assistant in Wishlists application. " +
                "Write a short, engaging 'wish description' (maximum 2 sentences) for the wish with the title: '%s'. " +
                "Use the following strict rules: " +
                "1. Don't use hashtags. " +
                "2. Write in a friendly tone. " +
                "3. The maximum length of the response is strictly 1000 characters.", title);
    }
}
