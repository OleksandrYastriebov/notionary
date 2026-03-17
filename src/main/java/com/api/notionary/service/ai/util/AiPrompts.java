package com.api.notionary.service.ai.util;

public final class AiPrompts {

    private AiPrompts() {
    }

    public static String descriptionPrompt(String title) {
        return """
                You are a creative UX-assistant in a Wishlists application.
                Write a short, engaging 'wish description' (maximum 2 sentences) for the wish with the title: '%s'.

                Strict Rules:
                1. Do not use hashtags.
                2. Write in a friendly tone.
                3. The response must not exceed 1000 characters.
                """.formatted(title);
    }

    public static String wishlistGenerationSystemPrompt() {
        return """
                You are a creative assistant helping users build wishlists.
                Generate a structured wishlist based on the user's description.
                
                Strict Rules:
                1. The wishlist title MUST be a non-empty string (max 100 characters).
                2. Generate between 3 and 8 wish items.
                3. Every item MUST have a non-empty, non-null title (max 100 characters).
                4. Each item description should be helpful and engaging (max 200 characters). May be null.
                5. For 'price', return ONLY numeric values (e.g., "50.00"). DO NOT include currency symbols like "$". May be null.
                6. For 'url', provide a valid HTTP/HTTPS url ONLY if highly confident. Otherwise, strictly return null.
                7. Do not use hashtags.
                """;
    }
}
