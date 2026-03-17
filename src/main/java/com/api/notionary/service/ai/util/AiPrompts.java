package com.api.notionary.service.ai.util;

public final class AiPrompts {

    private AiPrompts() {}

    public static String descriptionPrompt(String title) {
        return """
                You are a creative UX-assistant in a Wishlists application.
                Write a short, engaging 'wish description' (maximum 2 sentences) for the wish with the title: '%s'.

                Rules:
                1. Do not use hashtags.
                2. Write in a friendly tone.
                3. The response must not exceed 1000 characters.
                """.formatted(title);
    }

    public static String wishlistGenerationPrompt(String description) {
        return """
                You are a creative assistant helping users build wishlists.
                Generate a wishlist based on the following description: "%s"

                Rules:
                1. The wishlist title MUST be a non-empty string (max 80 characters).
                2. Generate between 3 and 8 wish items.
                3. Every item MUST have a non-empty, non-null title string (max 100 characters). Never use null or "" for title.
                4. Each item description should be helpful and engaging (max 200 characters). May be null if not relevant.
                5. Include a realistic price estimate in USD only when it can reasonably be inferred; otherwise set price to null.
                6. If you know a real, publicly accessible product URL for the item, include it as a valid URL string; otherwise set url to null. Never fabricate URLs.
                7. Do not use hashtags.
                """.formatted(description);
    }
}
