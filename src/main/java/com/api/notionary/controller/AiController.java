package com.api.notionary.controller;

import com.api.notionary.dto.ai.AiDescriptionDto;
import com.api.notionary.dto.payload.request.ai.GenerateDescriptionRequest;
import com.api.notionary.entity.User;
import com.api.notionary.security.interceptor.RateLimitPlan;
import com.api.notionary.security.interceptor.RateLimited;
import com.api.notionary.service.ai.AiAssistantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
