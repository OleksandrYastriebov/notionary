package com.api.wishoria.service.ai;

import com.api.wishoria.dto.payload.request.ai.GenerateDescriptionRequest;
import com.api.wishoria.entity.User;
import com.api.wishoria.entity.UserRole;
import com.api.wishoria.service.WishListItemService;
import com.api.wishoria.service.WishListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.mockito.ArgumentMatchers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private WishListService wishListService;

    @Mock
    private WishListItemService wishListItemService;

    private AiAssistantService aiAssistantService;

    private User user;
    private static final String WISHLIST_ID = "wl-abc123";
    private static final String GENERATED_DESCRIPTION = "A perfect gift for any occasion.";

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        aiAssistantService = new AiAssistantService(chatClient, wishListService, wishListItemService);

        user = new User("Jane", "Doe", "jane@wishoria.app", "hashed",
                Instant.now(), UserRole.ROLE_USER);
        user.setId(1L);
        user.setEnabled(true);
    }

    @Test
    void generateDescription_withTitleOnly_shouldCallTextResponse() {
        GenerateDescriptionRequest request = new GenerateDescriptionRequest("Sneakers Nike Air Max", null, null);
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn(GENERATED_DESCRIPTION);

        String result = aiAssistantService.generateDescription(request, WISHLIST_ID, user);

        assertThat(result).isEqualTo(GENERATED_DESCRIPTION);
        verify(wishListService).getWishlistEntityForOwner(WISHLIST_ID, user);
    }

    @Test
    void generateDescription_withNullImage_shouldCallTextResponse() {
        GenerateDescriptionRequest request = new GenerateDescriptionRequest("Camera Sony A7", null, null);
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn(GENERATED_DESCRIPTION);

        String result = aiAssistantService.generateDescription(request, WISHLIST_ID, user);

        assertThat(result).isEqualTo(GENERATED_DESCRIPTION);
    }

    @Test
    void generateDescription_withBlankImage_shouldCallTextResponse() {
        GenerateDescriptionRequest request = new GenerateDescriptionRequest("Headphones Sony WH-1000XM5", "   ", null);
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn(GENERATED_DESCRIPTION);

        String result = aiAssistantService.generateDescription(request, WISHLIST_ID, user);

        assertThat(result).isEqualTo(GENERATED_DESCRIPTION);
    }

    @Test
    void generateDescription_withTitleAndImage_shouldCallMultimodalResponse() {
        String base64Png = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        GenerateDescriptionRequest request = new GenerateDescriptionRequest(
                "Cool backpack", base64Png, "image/png");
        when(chatClient.prompt().user(ArgumentMatchers.<java.util.function.Consumer<ChatClient.PromptUserSpec>>any()).call().content())
                .thenReturn(GENERATED_DESCRIPTION);

        String result = aiAssistantService.generateDescription(request, WISHLIST_ID, user);

        assertThat(result).isEqualTo(GENERATED_DESCRIPTION);
        verify(wishListService).getWishlistEntityForOwner(WISHLIST_ID, user);
    }

    @Test
    void generateDescription_whenWishlistOwnerCheckFails_shouldPropagateException() {
        GenerateDescriptionRequest request = new GenerateDescriptionRequest("Watch", null, null);
        doThrow(new AccessDeniedException("You are not the owner"))
                .when(wishListService).getWishlistEntityForOwner(WISHLIST_ID, user);

        assertThatThrownBy(() -> aiAssistantService.generateDescription(request, WISHLIST_ID, user))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("owner");
    }

    @Test
    void generateDescription_whenChatClientThrows_shouldThrowRuntimeException() {
        GenerateDescriptionRequest request = new GenerateDescriptionRequest("Laptop Dell XPS", null, null);
        when(chatClient.prompt().user(anyString()).call().content())
                .thenThrow(new RuntimeException("AI service unavailable"));

        assertThatThrownBy(() -> aiAssistantService.generateDescription(request, WISHLIST_ID, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Impossible to generate description");
    }
}
