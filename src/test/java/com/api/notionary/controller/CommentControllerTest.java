package com.api.notionary.controller;

import com.api.notionary.dto.comment.CommentContainerDto;
import com.api.notionary.dto.comment.CommentDto;
import com.api.notionary.dto.payload.request.comment.CreateCommentRequest;
import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import com.api.notionary.exception.EntityNotFoundException;
import com.api.notionary.exception.GlobalExceptionHandler;
import com.api.notionary.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User authenticatedUser;
    private CommentDto sampleCommentDto;

    private static final String BASE_URL = "/api/v1/wishlists/wl-id-1/wishes/item-id-1/comments";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        authenticatedUser = new User("Alice", "Smith", "alice@notionary.app", "hashed",
                LocalDateTime.of(2024, 1, 1, 0, 0), UserRole.ROLE_USER);
        authenticatedUser.setId(5L);
        authenticatedUser.setEnabled(true);

        var auth = UsernamePasswordAuthenticationToken.authenticated(
                authenticatedUser, null, authenticatedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        sampleCommentDto = new CommentDto(1L, "Let's chip in!", 5L, "Alice", "Smith",
                null, LocalDateTime.of(2024, 6, 1, 10, 0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCommentsForItem_whenAuthenticated_shouldReturnCommentList() throws Exception {
        CommentContainerDto container = new CommentContainerDto(List.of(sampleCommentDto));
        when(commentService.getCommentsForItem(eq("wl-id-1"), eq("item-id-1"), any(User.class)))
                .thenReturn(container);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].id").value(1))
                .andExpect(jsonPath("$.comments[0].text").value("Let's chip in!"))
                .andExpect(jsonPath("$.comments[0].authorId").value(5));
    }

    @Test
    void getCommentsForItem_whenEmptyList_shouldReturnEmptyContainer() throws Exception {
        when(commentService.getCommentsForItem(eq("wl-id-1"), eq("item-id-1"), any(User.class)))
                .thenReturn(new CommentContainerDto(List.of()));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(0)));
    }

    @Test
    void getCommentsForItem_whenOwnerAttempts_shouldReturn403() throws Exception {
        when(commentService.getCommentsForItem(eq("wl-id-1"), eq("item-id-1"), any(User.class)))
                .thenThrow(new AccessDeniedException("Wishlist owner cannot view comments"));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCommentsForItem_whenItemNotFound_shouldReturn404() throws Exception {
        when(commentService.getCommentsForItem(eq("wl-id-1"), eq("ghost"), any()))
                .thenThrow(new EntityNotFoundException("Item not found"));

        mockMvc.perform(get("/api/v1/wishlists/wl-id-1/wishes/ghost/comments"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_whenValidRequest_shouldReturnCreatedComment() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Let's chip in!");
        when(commentService.createComment(eq("wl-id-1"), eq("item-id-1"),
                any(CreateCommentRequest.class), any(User.class)))
                .thenReturn(sampleCommentDto);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Let's chip in!"))
                .andExpect(jsonPath("$.authorFirstName").value("Alice"));
    }

    @Test
    void createComment_whenTextBlank_shouldReturn400() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_whenTextTooLong_shouldReturn400() throws Exception {
        String longText = "A".repeat(1001);
        CreateCommentRequest request = new CreateCommentRequest(longText);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_whenOwnerAttempts_shouldReturn403() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Some comment");
        when(commentService.createComment(eq("wl-id-1"), eq("item-id-1"), any(), any(User.class)))
                .thenThrow(new AccessDeniedException("Owner cannot comment on own wishlist"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createComment_whenItemNotFound_shouldReturn404() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Hello");
        when(commentService.createComment(eq("wl-id-1"), eq("item-id-1"), any(), any(User.class)))
                .thenThrow(new EntityNotFoundException("Item not found"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_whenBodyMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteComment_whenAuthor_shouldReturnOkMessage() throws Exception {
        doNothing().when(commentService).deleteComment(eq("wl-id-1"), eq("item-id-1"), eq(1L), any(User.class));

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment deleted successfully"));

        verify(commentService).deleteComment(eq("wl-id-1"), eq("item-id-1"), eq(1L), any(User.class));
    }

    @Test
    void deleteComment_whenNotAuthor_shouldReturn403() throws Exception {
        doThrow(new AccessDeniedException("Only the author can delete a comment"))
                .when(commentService).deleteComment(eq("wl-id-1"), eq("item-id-1"), eq(99L), any(User.class));

        mockMvc.perform(delete(BASE_URL + "/99"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteComment_whenCommentNotFound_shouldReturn404() throws Exception {
        doThrow(new EntityNotFoundException("Comment not found"))
                .when(commentService).deleteComment(eq("wl-id-1"), eq("item-id-1"), eq(999L), any(User.class));

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
