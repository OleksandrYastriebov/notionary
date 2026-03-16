package com.api.notionary.controller;

import com.api.notionary.dto.payload.request.ai.GenerateDescriptionRequest;
import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import com.api.notionary.exception.GlobalExceptionHandler;
import com.api.notionary.service.ai.AiAssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock
    private AiAssistantService aiAssistantService;

    @InjectMocks
    private AiController aiController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final String WISHLIST_ID = "wl-abc123";
    private static final String ENDPOINT = "/api/v1/ai/wishlists/" + WISHLIST_ID + "/generate-description";

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();

        mockMvc = MockMvcBuilders.standaloneSetup(aiController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User authenticatedUser = new User("John", "Doe", "john@notionary.app", "hashed",
                Instant.now(), UserRole.ROLE_USER);
        authenticatedUser.setId(10L);
        authenticatedUser.setEnabled(true);

        var auth = UsernamePasswordAuthenticationToken.authenticated(
                authenticatedUser, null, authenticatedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateDescription_whenValidRequest_shouldReturn200WithDescription() throws Exception {
        GenerateDescriptionRequest request = new GenerateDescriptionRequest("Nike Air Max", null, null);
        when(aiAssistantService.generateDescription(any(GenerateDescriptionRequest.class),
                eq(WISHLIST_ID), any(User.class)))
                .thenReturn("A stylish sneaker for everyday adventures.");

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("A stylish sneaker for everyday adventures."));
    }

    @Test
    void generateDescription_whenTitleTooLong_shouldReturn400() throws Exception {
        String longTitle = "A".repeat(101);
        GenerateDescriptionRequest request = new GenerateDescriptionRequest(longTitle, null, null);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateDescription_whenBodyMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateDescription_whenServiceThrowsRuntimeException_shouldReturn500() throws Exception {
        GenerateDescriptionRequest request = new GenerateDescriptionRequest("Laptop", null, null);
        when(aiAssistantService.generateDescription(any(GenerateDescriptionRequest.class),
                eq(WISHLIST_ID), any(User.class)))
                .thenThrow(new RuntimeException("Impossible to generate description. Please try again later."));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}
