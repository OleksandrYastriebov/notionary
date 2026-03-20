package com.api.wishoria.controller;

import com.api.wishoria.dto.user.PublicUserDto;
import com.api.wishoria.dto.user.UserAutocompleteDto;
import com.api.wishoria.dto.wishlist.WishListDto;
import com.api.wishoria.entity.User;
import com.api.wishoria.entity.UserRole;
import com.api.wishoria.exception.EntityNotFoundException;
import com.api.wishoria.exception.GlobalExceptionHandler;
import com.api.wishoria.service.UserService;
import com.api.wishoria.service.WishListService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private WishListService wishListService;

    @InjectMocks
    private ProfileController profileController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User authenticatedUser = new User("John", "Doe", "john@wishoria.app", "hashed",
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
    void searchUsers_whenValidQuery_shouldReturnOk() throws Exception {
        List<PublicUserDto> results = List.of(
                new PublicUserDto(1L, "Alice", "Brown", "https://example.com/avatar.jpg", "Loves reading", null),
                new PublicUserDto(2L, "Bob", "Green", null, null, null)
        );
        when(userService.searchPublicUsers(eq("alice"), any(User.class))).thenReturn(results);

        mockMvc.perform(get("/api/v1/profiles/search").param("q", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Alice"))
                .andExpect(jsonPath("$[1].firstName").value("Bob"));
    }

    @Test
    void searchUsers_whenQueryMissing_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void autocompleteUsers_whenValidQuery_shouldReturnOk() throws Exception {
        List<UserAutocompleteDto> results = List.of(
                new UserAutocompleteDto(1L, "Alice", "Brown", "alice@wishoria.app", "https://example.com/avatar.jpg")
        );
        when(userService.getUsersForAutocomplete(eq("ali"), any(User.class))).thenReturn(results);

        mockMvc.perform(get("/api/v1/profiles/autocomplete").param("q", "ali"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("alice@wishoria.app"));
    }

    @Test
    void autocompleteUsers_whenQueryMissing_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/autocomplete"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserProfile_whenExists_shouldReturnProfileWithWishlists() throws Exception {
        PublicUserDto publicUser = new PublicUserDto(5L, "Jane", "Smith", null, "Coffee lover", null);
        List<WishListDto> wishlists = List.of(
                new WishListDto("wl-1", 5L, List.of(), "Birthday Wishes", true, null, Instant.now())
        );

        when(userService.getPublicUserById(5L)).thenReturn(publicUser);
        when(wishListService.getAvailableWishlists(eq(5L), any(User.class))).thenReturn(wishlists);

        mockMvc.perform(get("/api/v1/profiles/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(5))
                .andExpect(jsonPath("$.user.firstName").value("Jane"))
                .andExpect(jsonPath("$.user.profileDescription").value("Coffee lover"))
                .andExpect(jsonPath("$.publicWishlists.length()").value(1))
                .andExpect(jsonPath("$.publicWishlists[0].title").value("Birthday Wishes"));
    }

    @Test
    void getUserProfile_whenUserNotFound_shouldReturn404() throws Exception {
        when(userService.getPublicUserById(999L)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/profiles/999"))
                .andExpect(status().isNotFound());
    }
}
